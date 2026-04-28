package com.officedubac.project.module.convocations.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResult {
    private String centreCode;
    private String status;
    private int totalCandidats;
    private int successCount;
    private int errorCount;
    private List<CentreResult> results;
    private long durationMs;

    public BatchResult(String centreCode, List<CentreResult> results) {
        this.centreCode = centreCode;
        this.results = results;
        this.successCount = (int) results.stream().filter(CentreResult::isSuccess).count();
        this.errorCount = results.size() - this.successCount;
        this.totalCandidats = results.size();
        this.status = errorCount == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
    }

    public BatchResult(String centreCode, String status) {
        this.centreCode = centreCode;
        this.status = status;
    }
}