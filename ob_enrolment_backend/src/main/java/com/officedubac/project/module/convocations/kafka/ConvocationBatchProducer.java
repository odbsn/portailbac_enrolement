package com.officedubac.project.module.convocations.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocationBatchProducer {

    private final KafkaTemplate<String, ConvocationBatchJob> kafkaTemplate;
    private final CandidatQueryService candidatQueryService;

    /**
     * Lancer un batch pour un centre
     */
    public String startBatchForCentre(String centreCode) {
        String batchId = UUID.randomUUID().toString();
        long total = candidatQueryService.countCandidatsByCentreCode(centreCode);

        ConvocationBatchJob job = new ConvocationBatchJob(
                batchId,
                centreCode,
                (int) total,
                System.currentTimeMillis(),
                ConvocationBatchJob.BatchType.CENTRE
        );

        kafkaTemplate.send("convocation.batch.request", centreCode, job);
        log.info("📨 Batch {} lancé pour centre {} ({} candidats)", batchId, centreCode, total);

        return batchId;
    }

    /**
     * Lancer un batch pour TOUS les centres
     */
    public String startBatchForAllCentres() {
        String batchId = UUID.randomUUID().toString();

        ConvocationBatchJob job = new ConvocationBatchJob(
                batchId,
                "ALL_CENTRES",
                0,
                System.currentTimeMillis(),
                ConvocationBatchJob.BatchType.ALL_CENTRES
        );

        kafkaTemplate.send("convocation.batch.request", "ALL_CENTRES", job);
        log.info("📨 Batch {} lancé pour TOUS les centres", batchId);

        return batchId;
    }
}
