package com.officedubac.project.module.candidatFinis;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.models.Ville;
import com.officedubac.project.repository.EtablissementRepository;
import com.officedubac.project.repository.VilleRepository;
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
public class CandidatImportService {

    private final MongoTemplate mongoTemplate;
    private final CandidatFinisRepository candidatFinisRepository;
    private final EtablissementRepository etablissementRepository;
    private final VilleRepository villeRepository;

    private static final int BATCH_SIZE = 10000;

    // Maps pour les établissements
    private Map<String, Etablissement> etablissementByNameMap;  // Recherche par nom
    private Map<String, Etablissement> etablissementByCodeMap;  // Recherche par code
    private Map<String, Ville> villeMap;  // Recherche par nom

    /**
     * Import avec EasyExcel - Version ultra-optimisée
     */
    @Transactional
    public ImportResult importFromExcel(MultipartFile file) {
        ImportResult result = new ImportResult();
        File tempFile = null;

        try {
            // 1. Pré-charger toutes les références existantes
            loadExistingReferences();

            log.info("========================================");
            log.info("PRÉ-CHARGEMENT TERMINÉ");
            log.info("Établissements (par nom): {}", etablissementByNameMap.size());
            log.info("Établissements (par code): {}", etablissementByCodeMap.size());
            log.info("Villes: {}", villeMap.size());
            log.info("========================================");

            // 2. Sauvegarder le fichier temporairement
            tempFile = File.createTempFile("excel_import_", ".xlsx");
            file.transferTo(tempFile);

            log.info("Début import: {}", file.getOriginalFilename());
            log.info("Taille fichier: {} MB", file.getSize() / (1024 * 1024));

            List<CandidatFinis> batchList = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            AtomicInteger totalProcessed = new AtomicInteger(0);
            AtomicInteger rowCount = new AtomicInteger(0);

            try (FileInputStream fis = new FileInputStream(tempFile)) {
                EasyExcel.read(fis, CandidatExcelDto.class, new AnalysisEventListener<CandidatExcelDto>() {

                            @Override
                            public void invoke(CandidatExcelDto dto, AnalysisContext context) {
                                rowCount.incrementAndGet();

                                try {
                                    ValidationResult validation = validateAllEtablissements(dto, result);

                                    if (!validation.isValid()) {
                                        result.addIgnored(rowCount.get(), validation.getReason());
                                        return;
                                    }

                                    CandidatFinis candidat = convertToEntityWithExistingRefs(dto, validation);

                                    if (isValidCandidat(candidat)) {
                                        synchronized (batchList) {
                                            batchList.add(candidat);

                                            if (batchList.size() >= BATCH_SIZE) {
                                                saveBatchBulkOptimized(batchList, result);
                                                int saved = batchList.size();
                                                batchList.clear();
                                                totalProcessed.addAndGet(saved);

                                                if (totalProcessed.get() % 50000 == 0) {
                                                    long elapsed = System.currentTimeMillis() - startTime;
                                                    double speed = totalProcessed.get() / (elapsed / 1000.0);
                                                    log.info("📊 Progression: {} lignes, {:.0f} lignes/sec",
                                                            totalProcessed.get(), speed);
                                                }
                                            }
                                        }
                                    } else {
                                        result.addError(rowCount.get(), "Données obligatoires manquantes (Numéro table ou nom)");
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
     * Analyse les lignes ignorées pour comprendre pourquoi elles ont été rejetées
     */
    public IgnoredAnalysisResult analyzeIgnoredRows(MultipartFile file) {
        IgnoredAnalysisResult analysis = new IgnoredAnalysisResult();
        File tempFile = null;

        try {
            loadExistingReferences();

            log.info("========================================");
            log.info("DÉBUT DE L'ANALYSE DES LIGNES IGNORÉES");
            log.info("========================================");

            tempFile = File.createTempFile("excel_analyze_", ".xlsx");
            file.transferTo(tempFile);

            AtomicInteger rowCount = new AtomicInteger(0);
            AtomicInteger totalIgnored = new AtomicInteger(0);

            try (FileInputStream fis = new FileInputStream(tempFile)) {
                EasyExcel.read(fis, CandidatExcelDto.class, new AnalysisEventListener<CandidatExcelDto>() {

                            @Override
                            public void invoke(CandidatExcelDto dto, AnalysisContext context) {
                                rowCount.incrementAndGet();

                                // Analyser chaque ligne
                                analyzeLine(dto, rowCount.get(), analysis);

                                if (rowCount.get() % 10000 == 0) {
                                    log.info("Analyse progression: {} lignes analysées, {} ignorées",
                                            rowCount.get(), analysis.getTotalIgnored());
                                }
                            }

                            @Override
                            public void doAfterAllAnalysed(AnalysisContext context) {
                                log.info("========================================");
                                log.info("ANALYSE TERMINÉE");
                                log.info("Total lignes analysées: {}", rowCount.get());
                                log.info("Total lignes ignorées: {}", analysis.getTotalIgnored());
                                log.info("========================================");

                                // Afficher les statistiques
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
    private void analyzeLine(CandidatExcelDto dto, int rowNumber, IgnoredAnalysisResult analysis) {
        List<String> reasons = new ArrayList<>();

        // Vérifier l'établissement principal
        String etablissementNom = safeTrim(dto.getEtablissement());
        if (etablissementNom == null || etablissementNom.isEmpty()) {
            reasons.add("Établissement principal manquant (vide)");
            analysis.addMissingEtablissementPrincipal(null);
        } else {
            Etablissement etab = etablissementByNameMap.get(etablissementNom.toLowerCase());
            if (etab == null) {
                reasons.add("Établissement principal non trouvé: '" + etablissementNom + "'");
                analysis.addMissingEtablissementPrincipal(etablissementNom);
            }
        }

        // Vérifier le centre d'écrit
        String centreEcritNom = safeTrim(dto.getCentreEcrit());
        if (centreEcritNom == null || centreEcritNom.isEmpty()) {
            reasons.add("Centre d'écrit manquant (vide)");
            analysis.addMissingCentreEcrit(null);
        } else {
            Etablissement centreEcrit = etablissementByNameMap.get(centreEcritNom.toLowerCase());
            if (centreEcrit == null) {
                reasons.add("Centre d'écrit non trouvé: '" + centreEcritNom + "'");
                analysis.addMissingCentreEcrit(centreEcritNom);
            }
        }

        // Vérifier le centre act EPS
        String centreActEPSCode = safeTrim(dto.getCentreActEPS());
        if (centreActEPSCode == null || centreActEPSCode.isEmpty()) {
            reasons.add("Centre act EPS manquant (vide)");
            analysis.addMissingCentreEPS(null);
        } else {
            Etablissement centreEPS = etablissementByCodeMap.get(centreActEPSCode.toLowerCase());
            if (centreEPS == null) {
                reasons.add("Centre act EPS non trouvé (code): '" + centreActEPSCode + "'");
                analysis.addMissingCentreEPS(centreActEPSCode);
            }
        }

        // Vérifier les données obligatoires
        String numeroTable = safeTrim(dto.getNumeroTable());
        if (numeroTable == null || numeroTable.isEmpty()) {
            reasons.add("Numéro de table manquant");
        }

        String nom = safeTrim(dto.getNom());
        if (nom == null || nom.isEmpty()) {
            reasons.add("Nom manquant");
        }

        // Si des raisons d'ignoré existent, les enregistrer
        if (!reasons.isEmpty()) {
            analysis.addIgnoredRow(rowNumber, dto, reasons);
        }
    }

    /**
     * Exporte les lignes ignorées vers un fichier CSV
     */
    public void exportIgnoredRowsToFile(ImportResult result, String fileName) {
        if (result.getIgnored().isEmpty()) {
            log.info("Aucune ligne ignorée à exporter");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Ligne,Raison\n");
            for (ImportResult.IgnoredRecord record : result.getIgnored()) {
                // Échapper les guillemets et les virgules
                String reason = record.reason().replace("\"", "\"\"");
                writer.write(String.format("%d,\"%s\"\n", record.row(), reason));
            }
            log.info("✅ Liste des {} lignes ignorées exportée vers {}", result.getIgnoredCount(), fileName);
        } catch (IOException e) {
            log.error("Erreur lors de l'export des ignorés", e);
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
            writer.write("Ligne,Numéro Table,Nom,Prénoms,Établissement Principal,Centre Écrit,Centre EPS (code),Raisons\n");
            for (IgnoredLine line : analysis.getIgnoredLines()) {
                CandidatExcelDto dto = line.getDto();
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        line.getRowNumber(),
                        safeCsv(dto.getNumeroTable()),
                        safeCsv(dto.getNom()),
                        safeCsv(dto.getPrenoms()),
                        safeCsv(dto.getEtablissement()),
                        safeCsv(dto.getCentreEcrit()),
                        safeCsv(dto.getCentreActEPS()),
                        String.join("; ", line.getReasons())
                ));
            }
            log.info("✅ Lignes ignorées exportées vers {}", ignoredFile);
        } catch (IOException e) {
            log.error("Erreur export lignes ignorées", e);
        }

        // Export des établissements manquants
        String missingFile = baseFileName + "_missing_establishments_" + timestamp + ".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(missingFile))) {
            writer.write("Type,Valeur,Occurrences\n");

            // Établissements principaux manquants
            analysis.getMissingEtablissementsPrincipaux().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            writer.write(String.format("Établissement principal,\"%s\",%d\n",
                                    entry.getKey() == null ? "VIDE" : entry.getKey(),
                                    entry.getValue()));
                        } catch (IOException e) {
                            log.error("Erreur écriture", e);
                        }
                    });

            // Centres d'écrit manquants
            analysis.getMissingCentresEcrit().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            writer.write(String.format("Centre d'écrit,\"%s\",%d\n",
                                    entry.getKey() == null ? "VIDE" : entry.getKey(),
                                    entry.getValue()));
                        } catch (IOException e) {
                            log.error("Erreur écriture", e);
                        }
                    });

            // Centres EPS manquants
            analysis.getMissingCentresEPS().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            writer.write(String.format("Centre EPS (code),\"%s\",%d\n",
                                    entry.getKey() == null ? "VIDE" : entry.getKey(),
                                    entry.getValue()));
                        } catch (IOException e) {
                            log.error("Erreur écriture", e);
                        }
                    });

            log.info("✅ Établissements manquants exportés vers {}", missingFile);
        } catch (IOException e) {
            log.error("Erreur export établissements manquants", e);
        }

        // Export des statistiques
        String statsFile = baseFileName + "_statistics_" + timestamp + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(statsFile))) {
            writer.write("=== STATISTIQUES DES LIGNES IGNORÉES ===\n");
            writer.write(String.format("Date: %s\n\n", LocalDateTime.now()));
            writer.write(String.format("Total lignes analysées: %d\n", analysis.getTotalAnalyzed()));
            writer.write(String.format("Total lignes ignorées: %d\n\n", analysis.getTotalIgnored()));

            writer.write("=== RÉPARTITION PAR CAUSE ===\n");
            analysis.getReasonCounts().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            writer.write(String.format("%s: %d (%.2f%%)\n",
                                    entry.getKey(),
                                    entry.getValue(),
                                    (entry.getValue() * 100.0) / analysis.getTotalIgnored()));
                        } catch (IOException e) {
                            log.error("Erreur écriture", e);
                        }
                    });

            writer.write("\n=== TOP 10 ÉTABLISSEMENTS PRINCIPAUX MANQUANTS ===\n");
            analysis.getMissingEtablissementsPrincipaux().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        try {
                            writer.write(String.format("\"%s\": %d\n",
                                    entry.getKey() == null ? "VIDE" : entry.getKey(),
                                    entry.getValue()));
                        } catch (IOException e) {
                            log.error("Erreur écriture", e);
                        }
                    });

            writer.write("\n=== TOP 10 CENTRES EPS MANQUANTS ===\n");
            analysis.getMissingCentresEPS().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        try {
                            writer.write(String.format("\"%s\": %d\n",
                                    entry.getKey() == null ? "VIDE" : entry.getKey(),
                                    entry.getValue()));
                        } catch (IOException e) {
                            log.error("Erreur écriture", e);
                        }
                    });

            log.info("✅ Statistiques exportées vers {}", statsFile);
        } catch (IOException e) {
            log.error("Erreur export statistiques", e);
        }
    }

    /**
     * Sauvegarde avec BulkOperations (LE PLUS PERFORMANT)
     */
    private void saveBatchBulkOptimized(List<CandidatFinis> batchList, ImportResult result) {
        if (batchList.isEmpty()) return;

        try {
            long start = System.currentTimeMillis();

            // 1. Récupérer tous les numéros existants en une seule requête
            List<String> numeroTables = batchList.stream()
                    .map(CandidatFinis::getNumeroTable)
                    .collect(Collectors.toList());

            Query query = Query.query(Criteria.where("numeroTable").in(numeroTables));
            List<CandidatFinis> existing = mongoTemplate.find(query, CandidatFinis.class);
            Set<String> existingNumbers = existing.stream()
                    .map(CandidatFinis::getNumeroTable)
                    .collect(Collectors.toSet());

            // 2. Filtrer les nouveaux
            List<CandidatFinis> toSave = batchList.stream()
                    .filter(c -> !existingNumbers.contains(c.getNumeroTable()))
                    .collect(Collectors.toList());

            // 3. Enregistrer les doublons
            batchList.stream()
                    .filter(c -> existingNumbers.contains(c.getNumeroTable()))
                    .forEach(c -> result.addDuplicate(c.getNumeroTable(), c.getJury()));

            // 4. BULK INSERT
            if (!toSave.isEmpty()) {
                BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, CandidatFinis.class);

                for (CandidatFinis candidat : toSave) {
                    bulkOps.insert(candidat);
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
     * Fallback en cas d'erreur bulk - insertion individuelle
     */
    private void saveBatchFallback(List<CandidatFinis> batchList, ImportResult result) {
        for (CandidatFinis candidat : batchList) {
            try {
                if (!candidatFinisRepository.existsByNumeroTable(candidat.getNumeroTable())) {
                    mongoTemplate.insert(candidat);
                    result.addImported(1);
                } else {
                    result.addDuplicate(candidat.getNumeroTable(), candidat.getJury());
                }
            } catch (Exception e) {
                log.error("Erreur insertion: {}", e.getMessage());
                result.addError(0, e.getMessage());
            }
        }
    }

    /**
     * Pré-charger tous les établissements et villes
     */
    private void loadExistingReferences() {
        // Charger tous les établissements
        List<Etablissement> etablissements = mongoTemplate.findAll(Etablissement.class);

        // Mapping par NOM (pour établissement principal et centre d'écrit)
        this.etablissementByNameMap = etablissements.stream()
                .filter(e -> e.getName() != null && !e.getName().isEmpty())
                .collect(Collectors.toMap(
                        e -> e.getName().trim().toLowerCase(),
                        e -> e,
                        (existing, replacement) -> existing
                ));

        // Mapping par CODE (pour centre act EPS)
        this.etablissementByCodeMap = etablissements.stream()
                .filter(e -> e.getCode() != null && !e.getCode().isEmpty())
                .collect(Collectors.toMap(
                        e -> e.getCode().trim().toLowerCase(),
                        e -> e,
                        (existing, replacement) -> existing
                ));

        // Charger toutes les villes
        List<Ville> villes = mongoTemplate.findAll(Ville.class);
        this.villeMap = villes.stream()
                .filter(v -> v.getName() != null && !v.getName().isEmpty())
                .collect(Collectors.toMap(
                        v -> v.getName().trim().toLowerCase(),
                        v -> v,
                        (existing, replacement) -> existing
                ));

        log.info("Références chargées - Établissements par nom: {}, par code: {}, Villes: {}",
                etablissementByNameMap.size(), etablissementByCodeMap.size(), villeMap.size());
    }

    /**
     * Valider que TOUS les établissements existent
     */
    private ValidationResult validateAllEtablissements(CandidatExcelDto dto, ImportResult result) {
        List<String> missingEtablissements = new ArrayList<>();

        // Validation par NOM pour l'établissement principal
        String etablissementNom = safeTrim(dto.getEtablissement());
        if (etablissementNom != null && !etablissementNom.isEmpty()) {
            if (etablissementByNameMap.get(etablissementNom.toLowerCase()) == null) {
                missingEtablissements.add("Établissement: '" + etablissementNom + "'");
                result.addMissingEtablissement(etablissementNom);
            }
        } else {
            result.addMissingEtablissement(null);
            return new ValidationResult(false, "Établissement principal manquant");
        }

        // Validation par NOM pour le centre d'écrit
        String centreEcritNom = safeTrim(dto.getCentreEcrit());
        if (centreEcritNom != null && !centreEcritNom.isEmpty()) {
            if (etablissementByNameMap.get(centreEcritNom.toLowerCase()) == null) {
                missingEtablissements.add("Centre d'écrit: '" + centreEcritNom + "'");
                result.addMissingCentreEcrit(centreEcritNom);
            }
        } else {
            result.addMissingCentreEcrit(null);
            return new ValidationResult(false, "Centre d'écrit manquant");
        }

        // Validation par CODE pour le centre act EPS
        String centreActEPSCode = safeTrim(dto.getCentreActEPS());
        if (centreActEPSCode != null && !centreActEPSCode.isEmpty()) {
            if (etablissementByCodeMap.get(centreActEPSCode.toLowerCase()) == null) {
                missingEtablissements.add("Centre act EPS (code): '" + centreActEPSCode + "'");
                result.addMissingCentreEPS(centreActEPSCode);
            }
        } else {
            result.addMissingCentreEPS(null);
            return new ValidationResult(false, "Centre act EPS manquant");
        }

        if (!missingEtablissements.isEmpty()) {
            return new ValidationResult(false, "Établissements non trouvés: " + String.join(", ", missingEtablissements));
        }

        return new ValidationResult(true, null);
    }

    /**
     * Convertit DTO en entité avec gestion sécurisée des types
     */
    private CandidatFinis convertToEntityWithExistingRefs(CandidatExcelDto dto, ValidationResult validation) {
        // Établissement principal - par NOM
        String etablissementNom = safeTrim(dto.getEtablissement());
        Etablissement etablissement = etablissementByNameMap.get(etablissementNom.toLowerCase());

        // Centre d'écrit - par NOM
        String centreEcritNom = safeTrim(dto.getCentreEcrit());
        Etablissement centreEcrit = etablissementByNameMap.get(centreEcritNom.toLowerCase());

        // Centre act EPS - par CODE
        String centreActEPSCode = safeTrim(dto.getCentreActEPS());
        Etablissement centreActEPS = etablissementByCodeMap.get(centreActEPSCode.toLowerCase());

        // Centre examen - par NOM (ville)
        String centreExamenNom = safeTrim(dto.getCentreExamen());
        Ville centreExamen = null;
        if (centreExamenNom != null && !centreExamenNom.isEmpty()) {
            centreExamen = villeMap.get(centreExamenNom.toLowerCase());
        }

        return CandidatFinis.builder()
                .prenoms(safeTrim(dto.getPrenoms()))
                .nom(safeTrim(dto.getNom()))
                .dateNaissance(safeTrim(dto.getDateNaissance()))
                .lieuNaissance(safeTrim(dto.getLieuNaissance()))
                .nationalite(safeTrim(dto.getNationalite()))
                .numeroTable(safeTrim(dto.getNumeroTable()))
                .jury(safeTrim(dto.getJury()))
                .serie(safeTrim(dto.getSerie()))
                .sexe(safeTrim(dto.getSexe()))
                .age(parseInteger(dto.getAge()))
                .eps(safeTrim(dto.getEps()))
                .numeroDossier(safeTrim(dto.getNumeroDossier()))
                .etablissement(etablissement)
                .centreExamen(centreExamen)
                .mo1(safeTrim(dto.getMo1()))
                .mo2(safeTrim(dto.getMo2()))
                .mo3(safeTrim(dto.getMo3()))
                .ef1(safeTrim(dto.getEf1()))
                .ef2(safeTrim(dto.getEf2()))
                .nbMatFacult(parseInteger(dto.getNbMatFacult()))
                .ia(parseInteger(dto.getIa()))
                .nti(parseInteger(dto.getNti()))
                .centreEcrit(centreEcrit)
                .codeCES(safeTrim(dto.getCodeCES()))
                .centreEcritParticulier(safeTrim(dto.getCentreEcritParticulier()))
                .statutResultat(safeTrim(dto.getStatutResultat()))
                .typeCandidat(safeTrim(dto.getTypeCandidat()))
                .codeEtatCivil(safeTrim(dto.getCodeEtatCivil()))
                .libEtatCivil(safeTrim(dto.getLibEtatCivil()))
                .anneeActe(safeTrim(dto.getAnneeActe()))
                .refActeNaissance(safeTrim(dto.getRefActeNaissance()))
                .dossierEnAttente(safeTrim(dto.getDossierEnAttente()))
                .resultat(safeTrim(dto.getResultat()))
                .raisonRejet(safeTrim(dto.getRaisonRejet()))
                .centreActEPS(centreActEPS)
                .datePassageEPS(safeTrim(dto.getDatePassageEPS()))
                .npEC(safeTrim(dto.getNpEC()))
                .idOrigine(safeTrim(dto.getIdOrigine()))
                .anneeODAE(safeTrim(dto.getAnneeODAE()))
                .paysODAE(safeTrim(dto.getPaysODAE()))
                .identifiantODAE(safeTrim(dto.getIdentifiantODAE()))
                .serieODAE(safeTrim(dto.getSerieODAE()))
                .codeEtsProvenance(safeTrim(dto.getCodeEtsProvenance()))
                .pasDeResultat(safeTrim(dto.getPasDeResultat()))
                .classeEtsProvenance(safeTrim(dto.getClasseEtsProvenance()))
                .departementProvenance(safeTrim(dto.getDepartementProvenance()))
                .departementVilleExamen(safeTrim(dto.getDepartementVilleExamen()))
                .candidatDeplace(safeTrim(dto.getCandidatDeplace()))
                .academieProvenance(safeTrim(dto.getAcademieProvenance()))
                .academieEcrit(safeTrim(dto.getAcademieEcrit()))
                .telephone(safeTrim(dto.getTelephone()))
                .handicap(safeTrim(dto.getHandicap()))
                .typeFiliere(safeTrim(dto.getTypeFiliere()))
                .sessionJury(safeTrim(dto.getSessionJury()))
                .moyenneFinale(parseDouble(dto.getMoyenneFinale()))
                .mention(safeTrim(dto.getMention()))
                .absence(safeTrim(dto.getAbsence()))
                .exclusion(safeTrim(dto.getExclusion()))
                .titreProjet(safeTrim(dto.getTitreProjet()))
                .groupeEts(safeTrim(dto.getGroupeEts()))
                .codeCentreSoutenance(safeTrim(dto.getCodeCentreSoutenance()))
                .libCentreSoutenance(safeTrim(dto.getLibCentreSoutenance()))
                .villeSoutenance(safeTrim(dto.getVilleSoutenance()))
                .centreMatFac1(safeTrim(dto.getCentreMatFac1()))
                .libMatFac1(safeTrim(dto.getLibMatFac1()))
                .villeMatFac1(safeTrim(dto.getVilleMatFac1()))
                .centreMatFac2(safeTrim(dto.getCentreMatFac2()))
                .libMatFac2(safeTrim(dto.getLibMatFac2()))
                .villeMatFac2(safeTrim(dto.getVilleMatFac2()))
                .build();
    }

    /**
     * Convertit une chaîne en Integer de manière sécurisée
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
     * Convertit une chaîne en Double de manière sécurisée
     */
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            String normalized = value.trim().replace(',', '.');
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            log.warn("Impossible de convertir '{}' en Double", value);
            return null;
        }
    }

    private String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }

    private String safeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private boolean isValidCandidat(CandidatFinis candidat) {
        return candidat.getNumeroTable() != null && !candidat.getNumeroTable().isEmpty()
                && candidat.getNom() != null && !candidat.getNom().isEmpty();
    }

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

        // Pour l'analyse des établissements manquants
        private final Map<String, Integer> missingEtablissementsPrincipaux = new HashMap<>();
        private final Map<String, Integer> missingCentresEcrit = new HashMap<>();
        private final Map<String, Integer> missingCentresEPS = new HashMap<>();

        public void addImported(int count) { this.importedCount += count; }

        public void addError(int row, String message) {
            this.errorRows++;
            this.errors.add(new ImportError(row, message));
        }

        public void addDuplicate(String numeroTable, String jury) {
            this.duplicates.add(new DuplicateRecord(numeroTable, jury));
        }

        public void addIgnored(int row, String reason) {
            this.ignoredCount++;
            this.ignored.add(new IgnoredRecord(row, reason));
        }

        public void addMissingEtablissement(String name) {
            String key = name == null ? "VIDE" : name;
            missingEtablissementsPrincipaux.merge(key, 1, Integer::sum);
        }

        public void addMissingCentreEcrit(String name) {
            String key = name == null ? "VIDE" : name;
            missingCentresEcrit.merge(key, 1, Integer::sum);
        }

        public void addMissingCentreEPS(String code) {
            String key = code == null ? "VIDE" : code;
            missingCentresEPS.merge(key, 1, Integer::sum);
        }

        public int getImportedCount() { return importedCount; }
        public int getErrorRows() { return errorRows; }
        public int getIgnoredCount() { return ignoredCount; }
        public List<ImportError> getErrors() { return errors; }
        public List<DuplicateRecord> getDuplicates() { return duplicates; }
        public List<IgnoredRecord> getIgnored() { return ignored; }
        public Map<String, Integer> getMissingEtablissementsPrincipaux() { return missingEtablissementsPrincipaux; }
        public Map<String, Integer> getMissingCentresEcrit() { return missingCentresEcrit; }
        public Map<String, Integer> getMissingCentresEPS() { return missingCentresEPS; }

        public record ImportError(int row, String message) {}
        public record DuplicateRecord(String numeroTable, String jury) {}
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
        private final Map<String, Integer> missingEtablissementsPrincipaux = new HashMap<>();
        private final Map<String, Integer> missingCentresEcrit = new HashMap<>();
        private final Map<String, Integer> missingCentresEPS = new HashMap<>();

        public void addIgnoredRow(int rowNumber, CandidatExcelDto dto, List<String> reasons) {
            totalIgnored++;
            ignoredLines.add(new IgnoredLine(rowNumber, dto, reasons));

            // Compter les raisons
            for (String reason : reasons) {
                reasonCounts.merge(reason, 1, Integer::sum);
            }
        }

        public void addMissingEtablissementPrincipal(String name) {
            String key = name == null ? "VIDE" : name;
            missingEtablissementsPrincipaux.merge(key, 1, Integer::sum);
        }

        public void addMissingCentreEcrit(String name) {
            String key = name == null ? "VIDE" : name;
            missingCentresEcrit.merge(key, 1, Integer::sum);
        }

        public void addMissingCentreEPS(String code) {
            String key = code == null ? "VIDE" : code;
            missingCentresEPS.merge(key, 1, Integer::sum);
        }

        public void setTotalAnalyzed(int total) {
            this.totalAnalyzed = total;
        }

        public void printStatistics() {
            log.info("=== STATISTIQUES DÉTAILLÉES ===");
            log.info("Total lignes ignorées: {}", totalIgnored);
            log.info("\nRépartition par cause:");
            reasonCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> log.info("  - {}: {} ({:.1f}%)",
                            entry.getKey(),
                            entry.getValue(),
                            (entry.getValue() * 100.0) / totalIgnored));

            log.info("\nTop 10 établissements principaux manquants:");
            missingEtablissementsPrincipaux.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> log.info("  - '{}': {}", entry.getKey(), entry.getValue()));

            log.info("\nTop 10 centres EPS manquants:");
            missingCentresEPS.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> log.info("  - '{}': {}", entry.getKey(), entry.getValue()));
        }

        public int getTotalAnalyzed() { return totalAnalyzed; }
        public int getTotalIgnored() { return totalIgnored; }
        public List<IgnoredLine> getIgnoredLines() { return ignoredLines; }
        public Map<String, Integer> getReasonCounts() { return reasonCounts; }
        public Map<String, Integer> getMissingEtablissementsPrincipaux() { return missingEtablissementsPrincipaux; }
        public Map<String, Integer> getMissingCentresEcrit() { return missingCentresEcrit; }
        public Map<String, Integer> getMissingCentresEPS() { return missingCentresEPS; }
    }

    /**
     * Classe pour stocker une ligne ignorée
     */
    public static class IgnoredLine {
        private final int rowNumber;
        private final CandidatExcelDto dto;
        private final List<String> reasons;

        public IgnoredLine(int rowNumber, CandidatExcelDto dto, List<String> reasons) {
            this.rowNumber = rowNumber;
            this.dto = dto;
            this.reasons = reasons;
        }

        public int getRowNumber() { return rowNumber; }
        public CandidatExcelDto getDto() { return dto; }
        public List<String> getReasons() { return reasons; }
    }
}