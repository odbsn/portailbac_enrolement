package com.officedubac.project.module.convocations.kafka;

import com.officedubac.project.module.candidatFinis.CandidatFinis;
import com.officedubac.project.module.convocations.ConvocationPdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocationBatchConsumer {
    private final ConvocationPdfService pdfService;
    private final CandidatQueryService candidatQueryService;
    private final KafkaTemplate<String, ConvocationBatchResult> resultKafkaTemplate;
    private final ConvocationStorageConfig storageConfig;  // ✅ Injection du config

    @KafkaListener(topics = "convocation.batch.request",
            groupId = "batch-workers",
            concurrency = "2")
    public void consumeBatch(ConvocationBatchJob job) {
        log.info("🎯 Démarrage batch {} pour centre: {}", job.getBatchId(), job.getCentreCode());

        // Envoyer statut STARTED
        sendStatus(job.getBatchId(), job.getCentreCode(), "STARTED", 0, 0, 0, 0);

        try {
            if (job.getType() == ConvocationBatchJob.BatchType.ALL_CENTRES) {
                processAllCentres(job);
            } else {
                processCentre(job);
            }
        } catch (Exception e) {
            log.error("❌ Batch {} échoué: {}", job.getBatchId(), e.getMessage());
            sendStatus(job.getBatchId(), job.getCentreCode(), "FAILED", 0, 0, 0, 0, e.getMessage());
        }
    }

    private void processCentre(ConvocationBatchJob job) {
        String centreCode = job.getCentreCode();
        List<CandidatFinis> candidats = candidatQueryService.findCandidatsByCentreCode(centreCode);

        int total = candidats.size();
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        // ✅ Utiliser storageConfig pour le chemin
        Path centrePath = storageConfig.getBasePath().resolve(sanitizeCode(centreCode));

        for (CandidatFinis candidat : candidats) {
            try {
                String numeroTable = candidat.getNumeroTable();
                byte[] pdfBytes = pdfService.generateConvocation(numeroTable).block();

                if (pdfBytes != null) {
                    Files.createDirectories(centrePath);
                    Path targetPath = centrePath.resolve(numeroTable + ".pdf");
                    Files.write(targetPath, pdfBytes);
                    success.incrementAndGet();
                } else {
                    errors.incrementAndGet();
                }
            } catch (Exception e) {
                log.error("Erreur pour {}: {}", candidat.getNumeroTable(), e.getMessage());
                errors.incrementAndGet();
            }

            int current = processed.incrementAndGet();

            // Envoyer statut toutes les 100 convocations
            if (current % 100 == 0) {
                sendStatus(job.getBatchId(), centreCode, "IN_PROGRESS",
                        total, current, success.get(), errors.get());
                log.info("📊 Batch {}: {}/{} (succès: {}, erreurs: {})",
                        job.getBatchId(), current, total, success.get(), errors.get());
            }
        }

        // Envoyer statut final
        sendStatus(job.getBatchId(), centreCode, "COMPLETED",
                total, total, success.get(), errors.get());
        log.info("✅ Batch {} terminé: {}/{} succès", job.getBatchId(), success.get(), total);
    }

    private void processAllCentres(ConvocationBatchJob job) {
        List<String> centres = candidatQueryService.findAllDistinctCentreCodes();

        for (String centreCode : centres) {
            // Créer un sous-batch pour chaque centre
            ConvocationBatchJob subJob = new ConvocationBatchJob(
                    job.getBatchId() + "_" + centreCode,
                    centreCode,
                    (int) candidatQueryService.countCandidatsByCentreCode(centreCode),
                    System.currentTimeMillis(),
                    ConvocationBatchJob.BatchType.CENTRE
            );
            processCentre(subJob);
        }

        sendStatus(job.getBatchId(), "ALL_CENTRES", "COMPLETED", 0, 0, 0, 0);
        log.info("✅ Batch {} terminé pour tous les centres", job.getBatchId());
    }

    private void sendStatus(String batchId, String centreCode, String status,
                            int total, int processed, int success, int errors) {
        sendStatus(batchId, centreCode, status, total, processed, success, errors, null);
    }

    private void sendStatus(String batchId, String centreCode, String status,
                            int total, int processed, int success, int errors, String errorMessage) {
        ConvocationBatchResult result = new ConvocationBatchResult(
                batchId, centreCode, status, total, processed, success, errors,
                System.currentTimeMillis(), errorMessage
        );
        resultKafkaTemplate.send("convocation.batch.status", batchId, result);
    }
    private String sanitizeCode(String code) {
        if (code == null) return "unknown";
        return code.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}