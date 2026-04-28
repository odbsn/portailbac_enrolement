package com.officedubac.project.module.convocations.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvocationBatchResult {
    private String batchId;
    private String centreCode;
    private String status; // STARTED, IN_PROGRESS, COMPLETED, FAILED
    private int total;
    private int processed;
    private int success;
    private int errors;
    private long durationMs;
    private String errorMessage;
}
