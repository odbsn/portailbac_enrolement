package com.officedubac.project.module.convocations.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvocationRegenerationEvent {
    private String numeroTable;
    private String oldNumeroTable;
    private String centreCode;
    private Long timestamp;
    private String reason;
}
