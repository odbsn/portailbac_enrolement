package com.officedubac.project.module.convocations.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/convocations/batch")
@RequiredArgsConstructor
public class ConvocationBatchController {

    private final ConvocationBatchProducer batchProducer;
    private final Map<String, ConvocationBatchResult> batchStatusMap = new ConcurrentHashMap<>();

    @PostMapping("/centre/{centreCode}")
    public ResponseEntity<Map<String, String>> startBatchByCentre(@PathVariable String centreCode) {
        String batchId = batchProducer.startBatchForCentre(centreCode);
        return ResponseEntity.ok(Map.of(
                "batchId", batchId,
                "message", "Batch lancé pour le centre: " + centreCode,
                "statusUrl", "/api/v1/convocations/batch/status/" + batchId
        ));
    }

    @PostMapping("/all-centres")
    public ResponseEntity<Map<String, String>> startBatchForAllCentres() {
        String batchId = batchProducer.startBatchForAllCentres();
        return ResponseEntity.ok(Map.of(
                "batchId", batchId,
                "message", "Batch lancé pour tous les centres",
                "statusUrl", "/api/v1/convocations/batch/status/" + batchId
        ));
    }

    @GetMapping("/status/{batchId}")
    public ResponseEntity<ConvocationBatchResult> getBatchStatus(@PathVariable String batchId) {
        // À implémenter avec un store (Redis, DB, ou cache)
        return ResponseEntity.ok(batchStatusMap.get(batchId));
    }

}