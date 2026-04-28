package com.officedubac.project.module.convocations.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CentreResult {
    private String numeroTable;
    private String filePath;
    private String errorMessage;
    private boolean success;
    private long durationMs;

    public static CentreResult success(String numeroTable, String filePath) {
        CentreResult result = new CentreResult();
        result.setNumeroTable(numeroTable);
        result.setFilePath(filePath);
        result.setSuccess(true);
        return result;
    }

    public static CentreResult error(String numeroTable, String errorMessage) {
        CentreResult result = new CentreResult();
        result.setNumeroTable(numeroTable);
        result.setErrorMessage(errorMessage);
        result.setSuccess(false);
        return result;
    }
}
