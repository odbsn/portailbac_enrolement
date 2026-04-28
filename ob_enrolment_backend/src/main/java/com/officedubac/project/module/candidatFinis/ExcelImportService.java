package com.officedubac.project.module.candidatFinis;

import com.officedubac.project.models.Etablissement;
import com.officedubac.project.models.Ville;
import com.officedubac.project.repository.EtablissementRepository;
import com.officedubac.project.repository.VilleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final CandidatFinisRepository candidatFinisRepository;
    private final EtablissementRepository etablissementRepository;
    private final VilleRepository villeRepository;

    private static final int BATCH_SIZE = 500;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Cache pour éviter les requêtes répétitives
    private final Map<String, Etablissement> etablissementCache = new HashMap<>();
    private final Map<String, Ville> villeCache = new HashMap<>();

    /**
     * Import des données depuis un fichier Excel avec gestion des erreurs et traitement par lots
     */
    @Transactional
    public ImportResult importFromExcel(String filePath) {
        ImportResult result = new ImportResult();
        List<CandidatFinis> batchList = new ArrayList<>();

        try (InputStream file = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> columnMap = getColumnMap(sheet.getRow(0));

            if (columnMap.isEmpty()) {
                throw new IllegalArgumentException("Le fichier Excel ne contient pas d'en-têtes valides");
            }

            log.info("Début de l'importation du fichier: {}", filePath);
            log.info("Colonnes trouvées: {}", columnMap.keySet());

            int totalRows = sheet.getLastRowNum();
            AtomicInteger processedRows = new AtomicInteger(0);
            AtomicInteger errorRows = new AtomicInteger(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // ignorer les en-têtes

                try {
                    CandidatFinis candidat = mapRowToCandidat(row, columnMap);

                    if (isValidCandidat(candidat)) {
                        batchList.add(candidat);
                        processedRows.incrementAndGet();
                    } else {
                        errorRows.incrementAndGet();
                        log.warn("Ligne {} ignorée: données incomplètes", row.getRowNum() + 1);
                        result.addError(row.getRowNum() + 1, "Données incomplètes");
                    }

                    // Sauvegarde par lots
                    if (batchList.size() >= BATCH_SIZE) {
                        saveBatch(batchList, result);
                        batchList.clear();
                        log.info("Progression: {}/{} lignes traitées", processedRows.get(), totalRows);
                    }

                } catch (Exception e) {
                    errorRows.incrementAndGet();
                    String errorMsg = String.format("Erreur ligne %d: %s", row.getRowNum() + 1, e.getMessage());
                    log.error(errorMsg);
                    result.addError(row.getRowNum() + 1, e.getMessage());
                }
            }

            // Sauvegarde du reste
            if (!batchList.isEmpty()) {
                saveBatch(batchList, result);
            }

            result.setTotalRows(totalRows);
            result.setProcessedRows(processedRows.get());
            result.setErrorRows(errorRows.get());

            log.info("Importation terminée. Total: {} lignes, Importées: {}, Erreurs: {}",
                    totalRows, result.getImportedCount(), errorRows.get());

        } catch (NotOfficeXmlFileException e) {
            log.error("Fichier Excel invalide ou corrompu: {}", e.getMessage());
            throw new RuntimeException("Le fichier n'est pas un fichier Excel valide", e);
        } catch (Exception e) {
            log.error("Erreur lors de l'importation: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur d'importation: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Sauvegarde d'un lot de candidats
     */
    private void saveBatch(List<CandidatFinis> batchList, ImportResult result) {
        try {
            // Vérifier les doublons avant sauvegarde
            List<CandidatFinis> toSave = new ArrayList<>();
            for (CandidatFinis candidat : batchList) {
                if (!candidatFinisRepository.existsByNumeroTable(
                        candidat.getNumeroTable())) {
                    toSave.add(candidat);
                } else {
                    log.warn("Doublon ignoré: Table {} - Jury {}",
                            candidat.getNumeroTable(), candidat.getJury());
                    result.addDuplicate(candidat.getNumeroTable(), candidat.getJury());
                }
            }

            if (!toSave.isEmpty()) {
                candidatFinisRepository.saveAll(toSave);
                result.addImported(toSave.size());
            }
        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde du lot: {}", e.getMessage());
            throw new RuntimeException("Erreur de sauvegarde", e);
        }
    }

    /**
     * Mapper une ligne Excel vers un objet CandidatFinis
     */
    private CandidatFinis mapRowToCandidat(Row row, Map<String, Integer> columnMap) {
        return CandidatFinis.builder()
                // IDENTITÉ
                .prenoms(getCellValueAsString(row.getCell(columnMap.get("Prénom(s)"))))
                .nom(getCellValueAsString(row.getCell(columnMap.get("Nom"))))
                .dateNaissance(parseDate(getCellValueAsString(row.getCell(columnMap.get("Date Nais.")))))
                .lieuNaissance(getCellValueAsString(row.getCell(columnMap.get("Lieu de naissance"))))
                .nationalite(getCellValueAsString(row.getCell(columnMap.get("Nationalité"))))

                // EXAMEN
                .numeroTable(getCellValueAsString(row.getCell(columnMap.get("N° Table"))))
                .jury(getCellValueAsString(row.getCell(columnMap.get("Jury"))))
                .serie(getCellValueAsString(row.getCell(columnMap.get("Série"))))
                .sexe(getCellValueAsString(row.getCell(columnMap.get("Se-\nxe"))))
                .age(getCellValueAsInteger(row.getCell(columnMap.get("Age"))))
                .eps(getCellValueAsString(row.getCell(columnMap.get("EPS"))))
                .numeroDossier(getCellValueAsString(row.getCell(columnMap.get("N°\nDos."))))

                // Établissement et centre d'examen
                .etablissement(getOrCreateEtablissement(getCellValueAsString(row.getCell(columnMap.get("Etablissement")))))
//                .centreExamen(getOrCreateVille(getCellValueAsString(row.getCell(columnMap.get("Centre d'examen")))))

                // MATIERES OPTIONNELLES
                .mo1(getCellValueAsString(row.getCell(columnMap.get("M.O-1"))))
                .mo2(getCellValueAsString(row.getCell(columnMap.get("M.O-2"))))
                .mo3(getCellValueAsString(row.getCell(columnMap.get("M.O-3"))))
                .ef1(getCellValueAsString(row.getCell(columnMap.get("E-F 1"))))
                .ef2(getCellValueAsString(row.getCell(columnMap.get("E-F 2"))))
                .nbMatFacult(getCellValueAsInteger(row.getCell(columnMap.get("Nb. Mat.\nFacult."))))

                .ia(getCellValueAsInteger(row.getCell(columnMap.get("I.A"))))
                .nti(getCellValueAsInteger(row.getCell(columnMap.get("N.T.I"))))
                .centreEcrit(getOrCreateEtablissement(getCellValueAsString(row.getCell(columnMap.get("Centre d'Ecrit")))))
                .codeCES(getCellValueAsString(row.getCell(columnMap.get("Code\nCES"))))
                .centreEcritParticulier(getCellValueAsString(row.getCell(columnMap.get("Crt. Ecrit\nParticulier"))))
                .statutResultat(getCellValueAsString(row.getCell(columnMap.get("S. R"))))
                .typeCandidat(getCellValueAsString(row.getCell(columnMap.get("Type\nCandidat"))))
                .codeEtatCivil(getCellValueAsString(row.getCell(columnMap.get("C.E.C"))))
                .libEtatCivil(getCellValueAsString(row.getCell(columnMap.get("Lib. Etat. Civ"))))
                .anneeActe(getCellValueAsString(row.getCell(columnMap.get("An. Acte."))))
                .refActeNaissance(getCellValueAsString(row.getCell(columnMap.get("Réf.Acte Nais."))))
                .dossierEnAttente(getCellValueAsString(row.getCell(columnMap.get("Dos. en\nAttentte"))))
                .resultat(getCellValueAsString(row.getCell(columnMap.get("Resultat"))))
                .raisonRejet(getCellValueAsString(row.getCell(columnMap.get("Raison\nRejet"))))
                .centreActEPS(getOrCreateEtablissement(getCellValueAsString(row.getCell(columnMap.get("Centre\nAct. EPS")))))
                .datePassageEPS(getCellValueAsString(row.getCell(columnMap.get("Date de \npassage EPS"))))
                .npEC(getCellValueAsString(row.getCell(columnMap.get("N.P.\nE.C"))))

                // ODAE
                .idOrigine(getCellValueAsString(row.getCell(columnMap.get("Id. d'origine"))))
                .anneeODAE(getCellValueAsString(row.getCell(columnMap.get("Année\nd'ODAE"))))
                .paysODAE(getCellValueAsString(row.getCell(columnMap.get("Pays\nd'ODAE"))))
                .identifiantODAE(getCellValueAsString(row.getCell(columnMap.get("Identifiant\nà l''ODAE"))))
                .serieODAE(getCellValueAsString(row.getCell(columnMap.get("Série\nà l'ODAE"))))

                // PROVENANCE
                .codeEtsProvenance(getCellValueAsString(row.getCell(columnMap.get("Code Ets.\nProvenance"))))
                .pasDeResultat(getCellValueAsString(row.getCell(columnMap.get("Pas de\nResultat"))))
                .classeEtsProvenance(getCellValueAsString(row.getCell(columnMap.get("Classe dans\nl'Ets. de prov."))))
                .departementProvenance(getCellValueAsString(row.getCell(columnMap.get("Département\nde provenance"))))
                .departementVilleExamen(getCellValueAsString(row.getCell(columnMap.get("Département de\nVille d'examen"))))
                .candidatDeplace(getCellValueAsString(row.getCell(columnMap.get("Candidat\ndéplacé"))))
                .academieProvenance(getCellValueAsString(row.getCell(columnMap.get("Acad. / Ets.\nProvenance"))))
                .academieEcrit(getCellValueAsString(row.getCell(columnMap.get("Acad. C.\nEcrit"))))
                .telephone(getCellValueAsString(row.getCell(columnMap.get("Tél. du \ncandidat"))))
                .handicap(getCellValueAsString(row.getCell(columnMap.get("Handicap"))))
                .typeFiliere(getCellValueAsString(row.getCell(columnMap.get("Type de Série\nou de /Filière"))))
                .sessionJury(getCellValueAsString(row.getCell(columnMap.get("Session\ndu Jury"))))
                .moyenneFinale(getCellValueAsDouble(row.getCell(columnMap.get("Moy.\nFinale"))))
                .mention(getCellValueAsString(row.getCell(columnMap.get("Mention"))))
                .absence(getCellValueAsString(row.getCell(columnMap.get("Abs."))))
                .exclusion(getCellValueAsString(row.getCell(columnMap.get("Exclu-\nsion"))))
                .titreProjet(getCellValueAsString(row.getCell(columnMap.get("Titre du sujet de projet\nde soutenance"))))
                .groupeEts(getCellValueAsString(row.getCell(columnMap.get("Groupe\n/Ets."))))
                .codeCentreSoutenance(getCellValueAsString(row.getCell(columnMap.get("Code Centre \nSoutenance"))))
                .libCentreSoutenance(getCellValueAsString(row.getCell(columnMap.get("Lib. centre de Soutenance"))))
                .villeSoutenance(getCellValueAsString(row.getCell(columnMap.get("Ville de soutenance"))))
                .centreMatFac1(getCellValueAsString(row.getCell(columnMap.get("centre pour\nmat. facult.1"))))
                .libMatFac1(getCellValueAsString(row.getCell(columnMap.get("Lib. mat. \nfacult. 1"))))
                .villeMatFac1(getCellValueAsString(row.getCell(columnMap.get("Lib. ville\nmat. facult. 1"))))
                .centreMatFac2(getCellValueAsString(row.getCell(columnMap.get("centre pour\nmat. facult.2"))))
                .libMatFac2(getCellValueAsString(row.getCell(columnMap.get("Lib. mat. \nfacult. 2"))))
                .villeMatFac2(getCellValueAsString(row.getCell(columnMap.get("Ville Mat.\n"))))
                .build();
    }

    /**
     * Map des colonnes avec leurs indices
     */
    private Map<String, Integer> getColumnMap(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                String cellValue = getCellValueAsString(cell);
                if (cellValue != null && !cellValue.isEmpty()) {
                    columnMap.put(cellValue.trim(), cell.getColumnIndex());
                }
            }
        }
        return columnMap;
    }

    /**
     * Récupère la valeur d'une cellule en String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        DataFormatter formatter = new DataFormatter();

        try {
            switch (cell.getCellType()) {
                case STRING:
                    String value = cell.getStringCellValue();
                    return (value != null && !value.trim().isEmpty()) ? value.trim() : null;

                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        try {
                            return cell.getLocalDateTimeCellValue()
                                    .toLocalDate()
                                    .format(DATE_FORMATTER);
                        } catch (Exception e) {
                            log.debug("Erreur format date: {}", e.getMessage());
                            return null;
                        }
                    }
                    return formatter.formatCellValue(cell);

                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());

                case FORMULA:
                    try {
                        return formatter.formatCellValue(cell);
                    } catch (Exception e) {
                        log.debug("Erreur formule: {}", e.getMessage());
                        return null;
                    }

                default:
                    return null;
            }
        } catch (Exception e) {
            log.debug("Erreur lecture cellule: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Récupère la valeur d'une cellule en Integer
     */
    private Integer getCellValueAsInteger(Cell cell) {
        String value = getCellValueAsString(cell);
        if (value == null || value.isEmpty()) return null;

        try {
            // Nettoyer la valeur (enlever les espaces, etc.)
            value = value.trim().replaceAll("[^0-9-]", "");
            return value.isEmpty() ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.debug("Conversion Integer impossible: {}", value);
            return null;
        }
    }

    /**
     * Récupère la valeur d'une cellule en Double
     */
    private Double getCellValueAsDouble(Cell cell) {
        String value = getCellValueAsString(cell);
        if (value == null || value.isEmpty()) return null;

        try {
            value = value.trim().replace(",", ".");
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.debug("Conversion Double impossible: {}", value);
            return null;
        }
    }

    /**
     * Parse une date
     */
    private String parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;

        try {
            // Essayer de parser la date
            LocalDate.parse(dateStr, DATE_FORMATTER);
            return dateStr;
        } catch (DateTimeParseException e) {
            log.debug("Format de date invalide: {}", dateStr);
            return null;
        }
    }

    /**
     * Valide les données minimales d'un candidat
     */
    private boolean isValidCandidat(CandidatFinis candidat) {
        return candidat.getNumeroTable() != null && !candidat.getNumeroTable().isEmpty()
                && candidat.getJury() != null && !candidat.getJury().isEmpty()
                && candidat.getNom() != null && !candidat.getNom().isEmpty();
    }

    /**
     * Récupère ou crée un établissement
     */
    private Etablissement getOrCreateEtablissement(String nom) {
        if (nom == null || nom.isEmpty()) return null;

        // Vérifier dans le cache
        if (etablissementCache.containsKey(nom)) {
            return etablissementCache.get(nom);
        }

        // Chercher en base de données
        Etablissement etablissement = etablissementRepository.findByNameIgnoreCase(nom)
                .orElseGet(() -> {
                    Etablissement newEtablissement = Etablissement.builder()
                            .name(nom)
                            .build();
                    return etablissementRepository.save(newEtablissement);
                });

        // Mettre en cache
        etablissementCache.put(nom, etablissement);

        return etablissement;
    }

    /**
     * Récupère ou crée une ville
     */
//    private Ville getOrCreateVille(String nom) {
//        if (nom == null || nom.isEmpty()) return null;
//
//        // Vérifier dans le cache
//        if (villeCache.containsKey(nom)) {
//            return villeCache.get(nom);
//        }
//
//        // Chercher en base de données
//        Ville ville = villeRepository.findByName(nom)
//                .orElseGet(() -> {
//                    Ville newVille = Ville.builder()
//                            .name(nom)
//                            .build();
//                    return villeRepository.save(newVille);
//                });
//
//        // Mettre en cache
//        villeCache.put(nom, ville);
//
//        return ville;
//    }

    /**
     * Classe pour le résultat de l'importation
     */
    public static class ImportResult {
        private int totalRows;
        private int processedRows;
        private int errorRows;
        private int importedCount;
        private final List<ImportError> errors = new ArrayList<>();
        private final List<DuplicateRecord> duplicates = new ArrayList<>();

        public void addImported(int count) {
            this.importedCount += count;
        }

        public void addError(int rowNumber, String message) {
            errors.add(new ImportError(rowNumber, message));
        }

        public void addDuplicate(String numeroTable, String jury) {
            duplicates.add(new DuplicateRecord(numeroTable, jury));
        }

        // Getters et Setters
        public int getTotalRows() { return totalRows; }
        public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
        public int getProcessedRows() { return processedRows; }
        public void setProcessedRows(int processedRows) { this.processedRows = processedRows; }
        public int getErrorRows() { return errorRows; }
        public void setErrorRows(int errorRows) { this.errorRows = errorRows; }
        public int getImportedCount() { return importedCount; }
        public List<ImportError> getErrors() { return errors; }
        public List<DuplicateRecord> getDuplicates() { return duplicates; }

        public static class ImportError {
            private final int rowNumber;
            private final String message;

            public ImportError(int rowNumber, String message) {
                this.rowNumber = rowNumber;
                this.message = message;
            }

            public int getRowNumber() { return rowNumber; }
            public String getMessage() { return message; }
        }

        public static class DuplicateRecord {
            private final String numeroTable;
            private final String jury;

            public DuplicateRecord(String numeroTable, String jury) {
                this.numeroTable = numeroTable;
                this.jury = jury;
            }

            public String getNumeroTable() { return numeroTable; }
            public String getJury() { return jury; }
        }
    }
}