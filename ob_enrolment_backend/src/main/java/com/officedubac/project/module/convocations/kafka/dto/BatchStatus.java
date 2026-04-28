package com.officedubac.project.module.convocations.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchStatus {
    private String centreCode;
    private int total;
    private int completed;
    private int failed;
    private double percentage;
    private String status; // RUNNING, COMPLETED, FAILED
    private long startTime;
    private long estimatedRemainingMs;

    public BatchStatus(String centreCode, int total) {
        this.centreCode = centreCode;
        this.total = total;
        this.completed = 0;
        this.failed = 0;
        this.percentage = 0;
        this.status = "RUNNING";
        this.startTime = System.currentTimeMillis();
    }

    public void increment() {
        this.completed++;
        this.percentage = (completed * 100.0) / total;
    }

    public void incrementFailed() {
        this.failed++;
    }

    public double getSpeed() {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed == 0) return 0;
        return (completed * 1000.0) / elapsed;
    }
}
