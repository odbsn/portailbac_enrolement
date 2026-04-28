package com.officedubac.project.module.convocations.kafka;

import com.officedubac.project.module.candidatFinis.CandidatFinis;
import com.officedubac.project.module.convocations.ConvocationPdfService;
import com.officedubac.project.module.convocations.kafka.dto.BatchResult;
import com.officedubac.project.module.convocations.kafka.dto.CentreResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocationBatchService {

    private final ConvocationPdfService pdfService;
    private final CandidatQueryService candidatQueryService;
    private final Path basePath = Paths.get("./convocations");

    /**
     * Générer toutes les convocations d'un centre d'écrit
     */
    public Mono<BatchResult> generateByCentre(String centreCode) {
        log.info("🚀 Génération batch pour centre: {}", centreCode);

        return Mono.fromCallable(() -> {
                    // Utiliser MongoTemplate pour récupérer les candidats
                    List<CandidatFinis> candidats = candidatQueryService.findCandidatsByCentreCode(centreCode);
                    List<CentreResult> results = new ArrayList<>();

                    if (candidats.isEmpty()) {
                        log.warn("⚠️ Aucun candidat trouvé pour le centre: {}", centreCode);
                        return new BatchResult(centreCode, results);
                    }

                    log.info("📄 {} candidats trouvés pour le centre: {}", candidats.size(), centreCode);

                    int count = 0;
                    int total = candidats.size();

                    for (CandidatFinis candidat : candidats) {
                        String numeroTable = candidat.getNumeroTable();
                        Path centrePath = basePath.resolve(sanitizeCentreCode(centreCode));

                        try {
                            byte[] pdfBytes = pdfService.generateConvocation(numeroTable).block();

                            if (pdfBytes != null && pdfBytes.length > 0) {
                                Files.createDirectories(centrePath);
                                Path targetPath = centrePath.resolve(numeroTable + ".pdf");
                                Files.write(targetPath, pdfBytes);
                                results.add(CentreResult.success(numeroTable, targetPath.toString()));
                                count++;

                                if (count % 50 == 0) {
                                    log.info("📊 Progression centre {}: {}/{}", centreCode, count, total);
                                }
                            } else {
                                results.add(CentreResult.error(numeroTable, "PDF généré vide"));
                            }
                        } catch (IOException e) {
                            log.error("❌ Erreur fichier pour {}: {}", numeroTable, e.getMessage());
                            results.add(CentreResult.error(numeroTable, "Erreur fichier: " + e.getMessage()));
                        } catch (Exception e) {
                            log.error("❌ Erreur génération pour {}: {}", numeroTable, e.getMessage());
                            results.add(CentreResult.error(numeroTable, e.getMessage()));
                        }
                    }

                    long successCount = results.stream().filter(CentreResult::isSuccess).count();
                    long errorCount = results.size() - successCount;

                    log.info("✅ Centre {} terminé: {} succès, {} erreurs", centreCode, successCount, errorCount);

                    return new BatchResult(centreCode, results);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Générer TOUS les centres
     */
    public Flux<BatchResult> generateAllCentres() {
        log.info("🚀 Démarrage génération pour tous les centres");

        return Mono.fromCallable(candidatQueryService::findAllDistinctCentreCodes)
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::generateByCentre, 2) // Max 2 centres en parallèle
                .doOnComplete(() -> log.info("✅ Génération de tous les centres terminée"))
                .doOnError(error -> log.error("❌ Erreur lors de la génération: {}", error.getMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private String sanitizeCentreCode(String code) {
        // Nettoyer le code pour éviter les problèmes de fichiers
        return code.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

}