package com.officedubac.project.module.convocations.kafka;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvocationBatchJob {
    private String batchId;
    private String centreCode;
    private int totalCandidats;
    private long startTime;
    private BatchType type;

    public enum BatchType {
        CENTRE, ALL_CENTRES
    }
}
