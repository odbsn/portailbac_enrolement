package com.officedubac.project.module.candidatFinis.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe pour stocker le résultat de l'importation Excel
 */
@Data
public class ImportResult {

    private int totalRows;           // Nombre total de lignes dans le fichier
    private int processedRows;       // Nombre de lignes traitées
    private int errorRows;           // Nombre de lignes en erreur
    private int importedCount;       // Nombre de lignes importées avec succès
    private int duplicateCount;      // Nombre de doublons ignorés

    private List<ImportError> errors = new ArrayList<>();
    private List<DuplicateRecord> duplicates = new ArrayList<>();

    public ImportResult() {
        this.totalRows = 0;
        this.processedRows = 0;
        this.errorRows = 0;
        this.importedCount = 0;
        this.duplicateCount = 0;
    }

    public void addImported(int count) {
        this.importedCount += count;
        this.processedRows += count;
    }

    public void addError(int rowNumber, String message) {
        this.errorRows++;
        this.errors.add(new ImportError(rowNumber, message));
    }

    public void addDuplicate(String numeroTable, String jury) {
        this.duplicateCount++;
        this.duplicates.add(new DuplicateRecord(numeroTable, jury));
    }

    public void addProcessedRow() {
        this.processedRows++;
    }

    /**
     * Retourne le pourcentage de réussite
     */
    public double getSuccessPercentage() {
        if (totalRows == 0) return 0;
        return (importedCount * 100.0) / totalRows;
    }

    /**
     * Retourne le pourcentage d'erreur
     */
    public double getErrorPercentage() {
        if (totalRows == 0) return 0;
        return (errorRows * 100.0) / totalRows;
    }

    /**
     * Vérifie si l'import a réussi (pas d'erreurs)
     */
    public boolean isSuccess() {
        return errorRows == 0 && importedCount > 0;
    }

    /**
     * Classe pour les erreurs d'import
     */
    @Data
    public static class ImportError {
        private int rowNumber;
        private String message;

        public ImportError(int rowNumber, String message) {
            this.rowNumber = rowNumber;
            this.message = message;
        }
    }

    /**
     * Classe pour les doublons détectés
     */
    @Data
    public static class DuplicateRecord {
        private String numeroTable;
        private String jury;

        public DuplicateRecord(String numeroTable, String jury) {
            this.numeroTable = numeroTable;
            this.jury = jury;
        }

        public String getKey() {
            return numeroTable + "-" + jury;
        }
    }

    @Override
    public String toString() {
        return String.format("ImportResult{totalRows=%d, importedCount=%d, errorRows=%d, duplicateCount=%d, successRate=%.2f%%}",
                totalRows, importedCount, errorRows, duplicateCount, getSuccessPercentage());
    }
}
