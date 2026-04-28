package com.officedubac.project.module.heure;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.officedubac.project.module.heure.dto.HeureExcelDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeureImportService {

    private final HeureRepository heureRepository;

    /**
     * Importe les heures depuis un fichier Excel
     */
    public ImportResult importFromExcel(MultipartFile file) {
        ImportResult result = new ImportResult();
        File tempFile = null;

        try {
            tempFile = File.createTempFile("heure_import_", ".xlsx");
            file.transferTo(tempFile);

            log.info("Début import des heures: {}", file.getOriginalFilename());

            Set<String> uniqueCodes = new HashSet<>();
            Map<String, String> heureMap = new HashMap<>();

            try (FileInputStream fis = new FileInputStream(tempFile)) {
                EasyExcel.read(fis, HeureExcelDto.class, new AnalysisEventListener<HeureExcelDto>() {

                            @Override
                            public void invoke(HeureExcelDto dto, AnalysisContext context) {
                                String code = safeTrim(dto.getCode());
                                String heure = safeTrim(dto.getHeure());

                                if (code != null && !code.isEmpty() && heure != null && !heure.isEmpty()) {
                                    uniqueCodes.add(code);
                                    heureMap.put(code, heure);
                                }
                            }

                            @Override
                            public void doAfterAllAnalysed(AnalysisContext context) {
                                log.info("Lecture terminée. {} heures uniques trouvées", uniqueCodes.size());
                            }

                            @Override
                            public void onException(Exception exception, AnalysisContext context) {
                                if (exception instanceof ExcelDataConvertException) {
                                    ExcelDataConvertException ex = (ExcelDataConvertException) exception;
                                    int rowIndex = ex.getRowIndex() + 1;
                                    log.error("❌ Erreur ligne {} colonne {}: {}", rowIndex, ex.getColumnIndex(), ex.getMessage());
                                    result.addError(rowIndex, "Erreur de conversion: " + ex.getMessage());
                                } else {
                                    log.error("❌ Erreur: {}", exception.getMessage());
                                    result.addError(0, exception.getMessage());
                                }
                            }
                        }).headRowNumber(1)
                        .ignoreEmptyRow(true)
                        .sheet()
                        .doRead();
            }

            // Traitement des heures uniques
            int importedCount = 0;
            int duplicateCount = 0;

            for (Map.Entry<String, String> entry : heureMap.entrySet()) {
                String code = entry.getKey();
                String heure = entry.getValue();
                int ordre = calculateOrdre(code);

                if (heureRepository.existsByCode(code)) {
                    log.debug("Heure déjà existante: {} -> {}", code, heure);
                    duplicateCount++;
                    result.addDuplicate(code, heure);
                } else {
                    Heure newHeure = Heure.builder()
                            .code(code)
                            .heure(heure)
                            .ordre(ordre)
                            .build();
                    heureRepository.save(newHeure);
                    importedCount++;
                    log.debug("Heure importée: {} -> {}", code, heure);
                }
            }

            result.setImportedCount(importedCount);
            result.setDuplicateCount(duplicateCount);

            log.info("✅ Import des heures terminé: {} importées, {} doublons", importedCount, duplicateCount);

        } catch (Exception e) {
            log.error("❌ Erreur fatale: {}", e.getMessage(), e);
            result.addError(0, "Erreur fatale: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.deleteIfExists(tempFile.toPath());
                } catch (IOException e) {
                    log.warn("Impossible de supprimer le fichier temporaire");
                }
            }
        }

        return result;
    }

    /**
     * Calcule l'ordre pour le tri chronologique
     * H1 = matin (1), H7 = après-midi (2), etc.
     */
    private int calculateOrdre(String code) {
        if (code == null) return 0;

        switch (code) {
            case "H1":
                return 1;
            case "H7":
                return 2;
            case "H8":
                return 3;
            default:
                // Pour les autres codes, extraire le numéro
                try {
                    return Integer.parseInt(code.substring(1));
                } catch (NumberFormatException e) {
                    return 99;
                }
        }
    }

    /**
     * Initialise les heures par défaut
     */
    public void initializeDefaultHeures() {
        log.info("Initialisation des heures par défaut...");

        if (heureRepository.count() > 0) {
            log.info("Les heures sont déjà initialisées. Count: {}", heureRepository.count());
            return;
        }

        List<Heure> heures = Arrays.asList(
                Heure.builder().code("H1").heure("07:30:00").ordre(1).build(),
                Heure.builder().code("H7").heure("14:30:00").ordre(2).build()
        );

        heureRepository.saveAll(heures);
        log.info("{} heures initialisées avec succès", heures.size());
    }

    private String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }

    // ==================== CLASSE DE RÉSULTAT ====================

    public static class ImportResult {
        private int importedCount = 0;
        private int duplicateCount = 0;
        private int errorCount = 0;
        private final List<ImportError> errors = new ArrayList<>();
        private final List<DuplicateRecord> duplicates = new ArrayList<>();

        public void addError(int row, String message) {
            errorCount++;
            errors.add(new ImportError(row, message));
        }

        public void addDuplicate(String code, String heure) {
            duplicates.add(new DuplicateRecord(code, heure));
        }

        public void setImportedCount(int count) { this.importedCount = count; }
        public void setDuplicateCount(int count) { this.duplicateCount = count; }

        public int getImportedCount() { return importedCount; }
        public int getDuplicateCount() { return duplicateCount; }
        public int getErrorCount() { return errorCount; }
        public List<ImportError> getErrors() { return errors; }
        public List<DuplicateRecord> getDuplicates() { return duplicates; }

        public record ImportError(int row, String message) {}
        public record DuplicateRecord(String code, String heure) {}
    }
}
