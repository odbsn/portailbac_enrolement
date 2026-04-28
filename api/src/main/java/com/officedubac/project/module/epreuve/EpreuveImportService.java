package com.officedubac.project.module.epreuve;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.officedubac.project.models.Matiere;
import com.officedubac.project.models.Serie;
import com.officedubac.project.module.epreuve.dto.EpreuveExcelDto;
import com.officedubac.project.module.heure.Heure;
import com.officedubac.project.module.heure.HeureRepository;
import com.officedubac.project.module.jour.Jour;
import com.officedubac.project.module.jour.JourRepository;
import com.officedubac.project.repository.MatiereRepository;
import com.officedubac.project.repository.SerieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpreuveImportService {

    private final MongoTemplate mongoTemplate;
    private final EpreuveRepository epreuveRepository;
    private final MatiereRepository matiereRepository;
    private final SerieRepository serieRepository;
    private final JourRepository jourRepository;
    private final HeureRepository heureRepository;

    private static final int BATCH_SIZE = 5000;

    // Maps pour les références
    private Map<String, Matiere> matiereMap;
    private Map<String, Serie> serieMap;
    private Map<String, Jour> jourMap;
    private Map<String, Heure> heureMap;

    /**
     * Import des épreuves depuis Excel
     */
    @Transactional
    public ImportResult importFromExcel(MultipartFile file) {
        ImportResult result = new ImportResult();
        File tempFile = null;

        try {
            // 1. Pré-charger toutes les références
            loadExistingReferences();

            log.info("========================================");
            log.info("PRÉ-CHARGEMENT DES RÉFÉRENCES TERMINÉ");
            log.info("Matières: {}", matiereMap.size());
            log.info("Séries: {}", serieMap.size());
            log.info("Jours: {}", jourMap.size());
            log.info("Heures: {}", heureMap.size());
            log.info("========================================");

            // 2. Sauvegarder le fichier temporairement
            tempFile = File.createTempFile("epreuve_import_", ".xlsx");
            file.transferTo(tempFile);

            log.info("Début import: {}", file.getOriginalFilename());
            log.info("Taille fichier: {} MB", file.getSize() / (1024 * 1024));

            List<Epreuve> batchList = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            AtomicInteger totalProcessed = new AtomicInteger(0);
            AtomicInteger rowCount = new AtomicInteger(0);

            try (FileInputStream fis = new FileInputStream(tempFile)) {
                EasyExcel.read(fis, EpreuveExcelDto.class, new AnalysisEventListener<EpreuveExcelDto>() {

                            @Override
                            public void invoke(EpreuveExcelDto dto, AnalysisContext context) {
                                rowCount.incrementAndGet();

                                try {
                                    // Valider les références
                                    ValidationResult validation = validateAllReferences(dto, result);

                                    if (!validation.isValid()) {
                                        result.addIgnored(rowCount.get(), validation.getReason());
                                        return;
                                    }

                                    // Convertir en entité
                                    Epreuve epreuve = convertToEntity(dto, validation);

                                    if (isValidEpreuve(epreuve)) {
                                        synchronized (batchList) {
                                            batchList.add(epreuve);

                                            if (batchList.size() >= BATCH_SIZE) {
                                                saveBatchBulkOptimized(batchList, result);
                                                int saved = batchList.size();
                                                batchList.clear();
                                                totalProcessed.addAndGet(saved);

                                                if (totalProcessed.get() % 20000 == 0) {
                                                    long elapsed = System.currentTimeMillis() - startTime;
                                                    double speed = totalProcessed.get() / (elapsed / 1000.0);
                                                    log.info("📊 Progression: {} lignes, {:.0f} lignes/sec",
                                                            totalProcessed.get(), speed);
                                                }
                                            }
                                        }
                                    } else {
                                        result.addError(rowCount.get(), "Données obligatoires manquantes (matière, série, type)");
                                    }
                                } catch (Exception e) {
                                    log.error("❌ Erreur ligne {}: {}", rowCount.get(), e.getMessage());
                                    result.addError(rowCount.get(), e.getMessage());
                                }
                            }

                            @Override
                            public void doAfterAllAnalysed(AnalysisContext context) {
                                if (!batchList.isEmpty()) {
                                    saveBatchBulkOptimized(batchList, result);
                                    totalProcessed.addAndGet(batchList.size());
                                    batchList.clear();
                                }

                                long elapsed = System.currentTimeMillis() - startTime;
                                log.info("========================================");
                                log.info("✅ IMPORT TERMINÉ EN {} secondes", elapsed / 1000);
                                log.info("✅ Importés: {} ({:.0f} lignes/sec)",
                                        result.getImportedCount(),
                                        result.getImportedCount() / (elapsed / 1000.0));
                                log.info("⚠️  Ignorés: {}", result.getIgnoredCount());
                                log.info("❌ Erreurs: {}", result.getErrorRows());
                                log.info("🔄 Doublons: {}", result.getDuplicates().size());
                                log.info("========================================");
                            }

                            @Override
                            public void onException(Exception exception, AnalysisContext context) {
                                if (exception instanceof ExcelDataConvertException) {
                                    ExcelDataConvertException ex = (ExcelDataConvertException) exception;
                                    int rowIndex = ex.getRowIndex() + 1;
                                    log.error("❌ Erreur ligne {} colonne {}: {}",
                                            rowIndex, ex.getColumnIndex(), ex.getMessage());
                                    result.addError(rowIndex, "Erreur de conversion: " + ex.getMessage());
                                } else {
                                    int rowIndex = context.readRowHolder() != null ?
                                            context.readRowHolder().getRowIndex() + 1 : 0;
                                    log.error("❌ Erreur ligne {}: {}", rowIndex, exception.getMessage());
                                    result.addError(rowIndex, exception.getMessage());
                                }
                            }
                        }).headRowNumber(1)
                        .ignoreEmptyRow(true)
                        .autoCloseStream(true)
                        .sheet()
                        .doRead();
            }

        } catch (Exception e) {
            log.error("❌ Erreur fatale: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur d'importation: " + e.getMessage(), e);
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
     * Analyse des lignes ignorées
     */
    public IgnoredAnalysisResult analyzeIgnoredRows(MultipartFile file) {
        IgnoredAnalysisResult analysis = new IgnoredAnalysisResult();
        File tempFile = null;

        try {
            loadExistingReferences();

            log.info("========================================");
            log.info("DÉBUT DE L'ANALYSE DES LIGNES IGNORÉES");
            log.info("========================================");

            tempFile = File.createTempFile("epreuve_analyze_", ".xlsx");
            file.transferTo(tempFile);

            AtomicInteger rowCount = new AtomicInteger(0);

            try (FileInputStream fis = new FileInputStream(tempFile)) {
                EasyExcel.read(fis, EpreuveExcelDto.class, new AnalysisEventListener<EpreuveExcelDto>() {

                            @Override
                            public void invoke(EpreuveExcelDto dto, AnalysisContext context) {
                                rowCount.incrementAndGet();
                                analyzeLine(dto, rowCount.get(), analysis);

                                if (rowCount.get() % 10000 == 0) {
                                    log.info("Analyse progression: {} lignes analysées, {} ignorées",
                                            rowCount.get(), analysis.getTotalIgnored());
                                }
                            }

                            @Override
                            public void doAfterAllAnalysed(AnalysisContext context) {
                                analysis.setTotalAnalyzed(rowCount.get());
                                log.info("========================================");
                                log.info("ANALYSE TERMINÉE");
                                log.info("Total lignes analysées: {}", analysis.getTotalAnalyzed());
                                log.info("Total lignes ignorées: {}", analysis.getTotalIgnored());
                                log.info("========================================");
                                analysis.printStatistics();
                            }
                        }).headRowNumber(1)
                        .ignoreEmptyRow(true)
                        .sheet()
                        .doRead();
            }

        } catch (Exception e) {
            log.error("Erreur lors de l'analyse: {}", e.getMessage(), e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.deleteIfExists(tempFile.toPath());
                } catch (IOException e) {
                    log.warn("Impossible de supprimer le fichier temporaire");
                }
            }
        }

        return analysis;
    }

    /**
     * Analyse une ligne individuelle
     */
    private void analyzeLine(EpreuveExcelDto dto, int rowNumber, IgnoredAnalysisResult analysis) {
        List<String> reasons = new ArrayList<>();

        // Vérifier matière
        String codeMatiere = safeTrim(dto.getMatiere());
        if (codeMatiere == null || codeMatiere.isEmpty()) {
            reasons.add("Code matière manquant");
            analysis.addMissingMatiere(null);
        } else {
            if (matiereMap.get(codeMatiere.toLowerCase()) == null) {
                reasons.add("Matière non trouvée: '" + codeMatiere + "'");
                analysis.addMissingMatiere(codeMatiere);
            }
        }

        // Vérifier série
        String codeSerie = safeTrim(dto.getSerie());
        if (codeSerie == null || codeSerie.isEmpty()) {
            reasons.add("Code série manquant");
            analysis.addMissingSerie(null);
        } else {
            if (serieMap.get(codeSerie.toLowerCase()) == null) {
                reasons.add("Série non trouvée: '" + codeSerie + "'");
                analysis.addMissingSerie(codeSerie);
            }
        }

        // Vérifier type
        String type = safeTrim(dto.getType());
        if (type == null || type.isEmpty()) {
            reasons.add("Type manquant");
        }

        // Vérifier jour (optionnel)
        String codeJour = safeTrim(dto.getJour());
        if (codeJour != null && !codeJour.isEmpty()) {
            if (jourMap.get(codeJour.toLowerCase()) == null) {
                reasons.add("Jour non trouvé: '" + codeJour + "'");
                analysis.addMissingJour(codeJour);
            }
        }

        // Vérifier heure (optionnel)
        String codeHeure = safeTrim(dto.getHeure());
        if (codeHeure != null && !codeHeure.isEmpty()) {
            if (heureMap.get(codeHeure.toLowerCase()) == null) {
                reasons.add("Heure non trouvée: '" + codeHeure + "'");
                analysis.addMissingHeure(codeHeure);
            }
        }

        // Si des raisons existent, enregistrer
        if (!reasons.isEmpty()) {
            analysis.addIgnoredRow(rowNumber, dto, reasons);
        }
    }

    /**
     * Exporte les analyses détaillées vers un fichier
     */
    public void exportAnalysisToFile(IgnoredAnalysisResult analysis, String baseFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));


        // Export des lignes ignorées
        String ignoredFile = baseFileName + "_ignored_lines_" + timestamp + ".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ignoredFile))) {
            writer.write("Ligne,Code Matière,Code Série,Type,Code Jour,Code Heure,Coef,Autorisation,Dominant,Nb Points,Durée,Raisons\n");
            for (IgnoredLine line : analysis.getIgnoredLines()) {
                EpreuveExcelDto dto = line.getDto();
                Boolean autorisation = null;
                String autorisationStr = safeTrim(dto.getAutorisation());
                if (autorisationStr != null && !autorisationStr.isEmpty()) {
                    autorisation = "OUI".equalsIgnoreCase(autorisationStr);
                } else {
                    autorisation = false;  // Valeur par défaut si vide
                }

                // Conversion de dominant (OUI/NON → Boolean)
                Boolean estDominant = null;
                String dominantStr = safeTrim(dto.getDominant());
                if (dominantStr != null && !dominantStr.isEmpty()) {
                    estDominant = "OUI".equalsIgnoreCase(dominantStr);
                } else {
                    estDominant = false;  // Valeur par défaut si vide
                }
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        line.getRowNumber(),
                        safeCsv(dto.getMatiere()),
                        safeCsv(dto.getSerie()),
                        safeCsv(dto.getType()),
                        safeCsv(dto.getJour()),
                        safeCsv(dto.getHeure()),
                        safeCsv(dto.getCoefficient()),
                        safeCsv(dto.getAutorisation()),
                        safeCsv(dto.getDominant()),
                        safeCsv(dto.getNombrePoints()),
                        safeCsv(dto.getDuree()),
                        String.join("; ", line.getReasons())
                ));
            }
            log.info("✅ Lignes ignorées exportées vers {}", ignoredFile);
        } catch (IOException e) {
            log.error("Erreur export lignes ignorées", e);
        }

        // Export des références manquantes
        String missingFile = baseFileName + "_missing_references_" + timestamp + ".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(missingFile))) {
            writer.write("Type,Valeur,Occurrences\n");

            analysis.getMissingMatieres().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            writer.write(String.format("Matière,\"%s\",%d\n",
                                    entry.getKey() == null ? "VIDE" : entry.getKey(),
                                    entry.getValue()));
                        } catch (IOException e) {
                            log.error("Erreur écriture", e);
                        }
                    });

            analysis.getMissingSeries().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            writer.write(String.format("Série,\"%s\",%d\n",
                                    entry.getKey() == null ? "VIDE" : entry.getKey(),
                                    entry.getValue()));
                        } catch (IOException e) {
                            log.error("Erreur écriture", e);
                        }
                    });

            log.info("✅ Références manquantes exportées vers {}", missingFile);
        } catch (IOException e) {
            log.error("Erreur export références manquantes", e);
        }
    }

    /**
     * Pré-charger toutes les références
     */
    private void loadExistingReferences() {
        // Charger les matières
        List<Matiere> matieres = mongoTemplate.findAll(Matiere.class);
        this.matiereMap = matieres.stream()
                .filter(m -> m.getCode() != null)
                .collect(Collectors.toMap(
                        m -> m.getCode().trim().toLowerCase(),
                        m -> m,
                        (existing, replacement) -> existing
                ));

        // Charger les séries
        List<Serie> series = mongoTemplate.findAll(Serie.class);
        this.serieMap = series.stream()
                .filter(s -> s.getCode() != null)
                .collect(Collectors.toMap(
                        s -> s.getCode().trim().toLowerCase(),
                        s -> s,
                        (existing, replacement) -> existing
                ));

        // Charger les jours
        List<Jour> jours = mongoTemplate.findAll(Jour.class);
        this.jourMap = jours.stream()
                .filter(j -> j.getCode() != null)
                .collect(Collectors.toMap(
                        j -> j.getCode().trim().toLowerCase(),
                        j -> j,
                        (existing, replacement) -> existing
                ));

        // Charger les heures
        List<Heure> heures = mongoTemplate.findAll(Heure.class);
        this.heureMap = heures.stream()
                .filter(h -> h.getCode() != null)
                .collect(Collectors.toMap(
                        h -> h.getCode().trim().toLowerCase(),
                        h -> h,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Valider toutes les références
     */
    private ValidationResult validateAllReferences(EpreuveExcelDto dto, ImportResult result) {
        List<String> missingReferences = new ArrayList<>();

        // Valider matière
        String codeMatiere = safeTrim(dto.getMatiere());
        if (codeMatiere != null && !codeMatiere.isEmpty()) {
            if (matiereMap.get(codeMatiere.toLowerCase()) == null) {
                missingReferences.add("Matière: '" + codeMatiere + "'");
                result.addMissingMatiere(codeMatiere);
            }
        } else {
            return new ValidationResult(false, "Code matière manquant");
        }

        // Valider série
        String codeSerie = safeTrim(dto.getSerie());
        if (codeSerie != null && !codeSerie.isEmpty()) {
            if (serieMap.get(codeSerie.toLowerCase()) == null) {
                missingReferences.add("Série: '" + codeSerie + "'");
                result.addMissingSerie(codeSerie);
            }
        } else {
            return new ValidationResult(false, "Code série manquant");
        }

        // Valider type
        String type = safeTrim(dto.getType());
        if (type == null || type.isEmpty()) {
            return new ValidationResult(false, "Type manquant");
        }

        // Valider jour (optionnel)
        String codeJour = safeTrim(dto.getJour());
        if (codeJour != null && !codeJour.isEmpty()) {
            if (jourMap.get(codeJour.toLowerCase()) == null) {
                missingReferences.add("Jour: '" + codeJour + "'");
                result.addMissingJour(codeJour);
            }
        }

        // Valider heure (optionnel)
        String codeHeure = safeTrim(dto.getHeure());
        if (codeHeure != null && !codeHeure.isEmpty()) {
            if (heureMap.get(codeHeure.toLowerCase()) == null) {
                missingReferences.add("Heure: '" + codeHeure + "'");
                result.addMissingHeure(codeHeure);
            }
        }

        if (!missingReferences.isEmpty()) {
            return new ValidationResult(false, "Références non trouvées: " + String.join(", ", missingReferences));
        }

        return new ValidationResult(true, null);
    }

    /**
     * Convertir DTO en entité Epreuve
     */
    private Epreuve convertToEntity(EpreuveExcelDto dto, ValidationResult validation) {
        String codeMatiere = safeTrim(dto.getMatiere());
        Matiere matiere = matiereMap.get(codeMatiere.toLowerCase());

        String codeSerie = safeTrim(dto.getSerie());
        Serie serie = serieMap.get(codeSerie.toLowerCase());

        Jour jour = null;
        String codeJour = safeTrim(dto.getJour());
        if (codeJour != null && !codeJour.isEmpty()) {
            jour = jourMap.get(codeJour.toLowerCase());
        }

        Heure heure = null;
        String codeHeure = safeTrim(dto.getHeure());
        if (codeHeure != null && !codeHeure.isEmpty()) {
            heure = heureMap.get(codeHeure.toLowerCase());
        }

        return Epreuve.builder()
                .matiere(matiere)
                .serie(serie)
                .coefficient(parseInteger(dto.getCoefficient()))
                .autorisation("OUI".equalsIgnoreCase(safeTrim(dto.getAutorisation())))
                .estDominant("OUI".equalsIgnoreCase(safeTrim(dto.getDominant())))
                .nombrePoints(parseInteger(dto.getNombrePoints()))
                .jourDebut(jour)
                .heureDebut(heure)
                .duree(safeTrim(dto.getDuree()))
                .type(safeTrim(dto.getType()))
                .build();
    }


    /**
     * Sauvegarde avec BulkOperations
     */
    private void saveBatchBulkOptimized(List<Epreuve> batchList, ImportResult result) {
        if (batchList.isEmpty()) return;

        try {
            long start = System.currentTimeMillis();

            // Vérifier les doublons (matière + série + type)
            List<String> compositeKeys = batchList.stream()
                    .map(e -> e.getMatiere().getCode() + "|" + e.getSerie().getCode() + "|" + e.getType())
                    .collect(Collectors.toList());

            Query query = Query.query(Criteria.where("compositeKey").in(compositeKeys));
            List<Epreuve> existing = mongoTemplate.find(query, Epreuve.class);
            Set<String> existingKeys = existing.stream()
                    .map(e -> e.getMatiere().getCode() + "|" + e.getSerie().getCode() + "|" + e.getType())
                    .collect(Collectors.toSet());

            // Filtrer les nouveaux
            List<Epreuve> toSave = batchList.stream()
                    .filter(e -> !existingKeys.contains(e.getMatiere().getCode() + "|" + e.getSerie().getCode() + "|" + e.getType()))
                    .collect(Collectors.toList());

            // Enregistrer les doublons
            batchList.stream()
                    .filter(e -> existingKeys.contains(e.getMatiere().getCode() + "|" + e.getSerie().getCode() + "|" + e.getType()))
                    .forEach(e -> result.addDuplicate(e.getMatiere().getCode() + " - " + e.getSerie().getCode(), e.getType()));

            // Bulk insert
            if (!toSave.isEmpty()) {
                BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Epreuve.class);

                for (Epreuve epreuve : toSave) {
                    bulkOps.insert(epreuve);
                }

                bulkOps.execute();
                result.addImported(toSave.size());

                long duration = System.currentTimeMillis() - start;
                log.debug("⚡ Bulk insert: {} lignes en {} ms ({:.0f} lignes/sec)",
                        toSave.size(), duration, toSave.size() / (duration / 1000.0));
            }

        } catch (Exception e) {
            log.error("❌ Erreur bulk insert: {}", e.getMessage(), e);
            saveBatchFallback(batchList, result);
        }
    }

    /**
     * Fallback en cas d'erreur bulk
     */
    private void saveBatchFallback(List<Epreuve> batchList, ImportResult result) {
        for (Epreuve epreuve : batchList) {
            try {
                mongoTemplate.insert(epreuve);
                result.addImported(1);
            } catch (Exception e) {
                log.error("Erreur insertion: {}", e.getMessage());
                result.addError(0, e.getMessage());
            }
        }
    }

    /**
     * Vérifie si une épreuve est valide
     */
    private boolean isValidEpreuve(Epreuve epreuve) {
        return epreuve.getMatiere() != null
                && epreuve.getSerie() != null
                && epreuve.getType() != null
                && !epreuve.getType().isEmpty();
    }

    /**
     * Convertit une chaîne en Integer
     */
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Impossible de convertir '{}' en Integer", value);
            return null;
        }
    }

    /**
     * Nettoie une chaîne
     */
    private String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }

    /**
     * Échappe les caractères CSV
     */
    private String safeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    // ==================== CLASSES INTERNES ====================

    private static class ValidationResult {
        private final boolean valid;
        private final String reason;

        public ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public boolean isValid() { return valid; }
        public String getReason() { return reason; }
    }

    public static class ImportResult {
        private int importedCount = 0;
        private int errorRows = 0;
        private int ignoredCount = 0;
        private final List<ImportError> errors = new ArrayList<>();
        private final List<DuplicateRecord> duplicates = new ArrayList<>();
        private final List<IgnoredRecord> ignored = new ArrayList<>();

        private final Map<String, Integer> missingMatieres = new HashMap<>();
        private final Map<String, Integer> missingSeries = new HashMap<>();
        private final Map<String, Integer> missingJours = new HashMap<>();
        private final Map<String, Integer> missingHeures = new HashMap<>();

        public void addImported(int count) { this.importedCount += count; }
        public void addError(int row, String message) { this.errorRows++; this.errors.add(new ImportError(row, message)); }
        public void addDuplicate(String key, String type) { this.duplicates.add(new DuplicateRecord(key, type)); }
        public void addIgnored(int row, String reason) { this.ignoredCount++; this.ignored.add(new IgnoredRecord(row, reason)); }

        public void addMissingMatiere(String code) { missingMatieres.merge(code == null ? "VIDE" : code, 1, Integer::sum); }
        public void addMissingSerie(String code) { missingSeries.merge(code == null ? "VIDE" : code, 1, Integer::sum); }
        public void addMissingJour(String code) { missingJours.merge(code == null ? "VIDE" : code, 1, Integer::sum); }
        public void addMissingHeure(String code) { missingHeures.merge(code == null ? "VIDE" : code, 1, Integer::sum); }

        public int getImportedCount() { return importedCount; }
        public int getErrorRows() { return errorRows; }
        public int getIgnoredCount() { return ignoredCount; }
        public List<ImportError> getErrors() { return errors; }
        public List<DuplicateRecord> getDuplicates() { return duplicates; }
        public List<IgnoredRecord> getIgnored() { return ignored; }
        public Map<String, Integer> getMissingMatieres() { return missingMatieres; }
        public Map<String, Integer> getMissingSeries() { return missingSeries; }
        public Map<String, Integer> getMissingJours() { return missingJours; }
        public Map<String, Integer> getMissingHeures() { return missingHeures; }

        public record ImportError(int row, String message) {}
        public record DuplicateRecord(String key, String type) {}
        public record IgnoredRecord(int row, String reason) {}
    }

    /**
     * Classe pour stocker les résultats de l'analyse des lignes ignorées
     */
    public static class IgnoredAnalysisResult {
        private int totalAnalyzed = 0;
        private int totalIgnored = 0;
        private final List<IgnoredLine> ignoredLines = new ArrayList<>();
        private final Map<String, Integer> reasonCounts = new HashMap<>();
        private final Map<String, Integer> missingMatieres = new HashMap<>();
        private final Map<String, Integer> missingSeries = new HashMap<>();
        private final Map<String, Integer> missingJours = new HashMap<>();
        private final Map<String, Integer> missingHeures = new HashMap<>();

        public void addIgnoredRow(int rowNumber, EpreuveExcelDto dto, List<String> reasons) {
            totalIgnored++;
            ignoredLines.add(new IgnoredLine(rowNumber, dto, reasons));
            for (String reason : reasons) {
                reasonCounts.merge(reason, 1, Integer::sum);
            }
        }

        public void addMissingMatiere(String code) { missingMatieres.merge(code == null ? "VIDE" : code, 1, Integer::sum); }
        public void addMissingSerie(String code) { missingSeries.merge(code == null ? "VIDE" : code, 1, Integer::sum); }
        public void addMissingJour(String code) { missingJours.merge(code == null ? "VIDE" : code, 1, Integer::sum); }
        public void addMissingHeure(String code) { missingHeures.merge(code == null ? "VIDE" : code, 1, Integer::sum); }

        public void setTotalAnalyzed(int total) { this.totalAnalyzed = total; }

        public void printStatistics() {
            log.info("=== STATISTIQUES DÉTAILLÉES ===");
            log.info("Total lignes ignorées: {}", totalIgnored);
            if (totalIgnored > 0) {
                log.info("\nRépartition par cause:");
                reasonCounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .forEach(entry -> log.info("  - {}: {} ({:.1f}%)",
                                entry.getKey(), entry.getValue(),
                                (entry.getValue() * 100.0) / totalIgnored));

                log.info("\nTop 10 matières manquantes:");
                missingMatieres.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(10)
                        .forEach(entry -> log.info("  - '{}': {}", entry.getKey(), entry.getValue()));
            }
        }

        public int getTotalAnalyzed() { return totalAnalyzed; }
        public int getTotalIgnored() { return totalIgnored; }
        public List<IgnoredLine> getIgnoredLines() { return ignoredLines; }
        public Map<String, Integer> getReasonCounts() { return reasonCounts; }
        public Map<String, Integer> getMissingMatieres() { return missingMatieres; }
        public Map<String, Integer> getMissingSeries() { return missingSeries; }
        public Map<String, Integer> getMissingJours() { return missingJours; }
        public Map<String, Integer> getMissingHeures() { return missingHeures; }
    }

    /**
     * Classe pour stocker une ligne ignorée
     */
    public static class IgnoredLine {
        private final int rowNumber;
        private final EpreuveExcelDto dto;
        private final List<String> reasons;

        public IgnoredLine(int rowNumber, EpreuveExcelDto dto, List<String> reasons) {
            this.rowNumber = rowNumber;
            this.dto = dto;
            this.reasons = reasons;
        }

        public int getRowNumber() { return rowNumber; }
        public EpreuveExcelDto getDto() { return dto; }
        public List<String> getReasons() { return reasons; }
    }
}