package com.officedubac.project.module.nouveauBachelier;

import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.exception.ResourceAlreadyExists;
import com.officedubac.project.models.Jury;
import com.officedubac.project.module.nouveauBachelier.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NouveauBachelierServiceImp implements NouveauBachelierService {
    private final NouveauBachelierMapper mapper;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final MongoTemplate mongoTemplate;
    private final NouveauBachelierDao dao;

    @Override
    public List<NouveauBachelierResponse> all() throws BusinessResourceException {
        log.info("=== all() - Début récupération de tous les bacheliers ===");
        List<NouveauBachelier> all = dao.findAll();
        log.info("Nombre de bacheliers trouvés: {}", all.size());
        List<NouveauBachelierResponse> response = all.stream()
                .map(mapper::entiteToResponse)
                .collect(Collectors.toList());
        log.info("=== all() - Fin, {} bacheliers retournés ===", response.size());
        return response;
    }

    @Override
    public Optional<NouveauBachelierResponse> oneById(String id) throws NumberFormatException, BusinessResourceException {
        log.info("=== oneById() - Recherche bachelier avec id: {} ===", id);
        try {
            NouveauBachelier nouveauBachelier = mongoTemplate.findById(id, NouveauBachelier.class);
            log.info("Bachelier avec id: {} trouvé: {}", id, nouveauBachelier != null);
            Optional<NouveauBachelierResponse> response = Optional.ofNullable(mapper.entiteToResponse(nouveauBachelier));
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramètre id {} non autorisé. <oneById>.", id);
            throw new BusinessResourceException("not-valid-param", "Paramètre " + id + " non autorisé.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche du bachelier avec id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public NouveauBachelierResponse add(NouveauBachelierRequest req) throws BusinessResourceException {
        log.info("=== add() - Création d'un nouveau bachelier ===");
        log.debug("Request: {}", req.toString());
        try {
            NouveauBachelier one = mapper.requestToEntity(req);
            log.debug("Entity mappée: {}", one.toString());
            NouveauBachelierResponse response = mapper.entiteToResponse(dao.save(one));
            log.info("Bachelier avec numéro table {} ajouté avec succès", response.getNumeroTable());
            return response;
        } catch (ResourceAlreadyExists | DataIntegrityViolationException e) {
            log.error("Erreur de création: donnée en doublon ou contrainte non respectée", e);
            throw new BusinessResourceException("data-error", "Donnée en doublon ou contrainte non respectée ", HttpStatus.CONFLICT);
        } catch (Exception ex) {
            log.error("Erreur technique lors de l'ajout: {}", ex.getMessage(), ex);
            throw new BusinessResourceException("technical-error", "Erreur technique de création d'un NouveauBachelier: " + req.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public NouveauBachelierResponse maj(NouveauBachelierRequest req, String id) throws NumberFormatException, NoSuchElementException, BusinessResourceException {
        log.info("=== maj() - Mise à jour du bachelier avec id: {} ===", id);
        try {
            NouveauBachelier NouveauBachelierOptional = dao.findById(id)
                    .orElseThrow(() -> new BusinessResourceException("not-found", "Aucun NouveauBachelier avec " + id + " trouvé.", HttpStatus.NOT_FOUND));

            NouveauBachelier oneBrute = mapper.requestToEntiteUp(NouveauBachelierOptional, req);
            NouveauBachelierResponse response = mapper.entiteToResponse(dao.save(oneBrute));
            log.info("Bachelier avec numéro table {} mis à jour avec succès", response.getNumeroTable());
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramètre id {} non autorisé. <maj>.", id);
            throw new BusinessResourceException("not-valid-param", "Paramètre " + id + " non autorisé.", HttpStatus.BAD_REQUEST);
        } catch (ResourceAlreadyExists | DataIntegrityViolationException e) {
            log.error("Erreur technique de mise à jour: donnée en doublon ou contrainte non respectée", e);
            throw new BusinessResourceException("data-error", "Donnée en doublon ou contrainte non respectée ", HttpStatus.CONFLICT);
        } catch (Exception ex) {
            log.error("Erreur technique lors de la mise à jour: {}", ex.getMessage(), ex);
            throw new BusinessResourceException("technical-error", "Erreur technique de mise à jour d'un NouveauBachelier: " + req.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public String del(String id) throws NumberFormatException, BusinessResourceException {
        log.info("=== del() - Suppression du bachelier avec id: {} ===", id);
        try {
            NouveauBachelier oneBrute = dao.findById(id)
                    .orElseThrow(() -> new BusinessResourceException("not-found", "Aucun NouveauBachelier avec " + id + " trouvé.", HttpStatus.NOT_FOUND));

            dao.deleteById(id);
            log.info("Bachelier avec id: {} et numéro table: {} supprimé avec succès", id, oneBrute.getNumeroTable());
            return "Imputation: " + oneBrute.getNumeroTable() + " supprimé avec succès. <del>";
        } catch (NumberFormatException e) {
            log.warn("Paramètre id {} non autorisé. <del>.", id);
            throw new BusinessResourceException("not-valid-param", "Paramètre " + id + " non autorisé.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Optional<NouveauBachelierResponse> searchSimple(String numeroTable) throws BusinessResourceException {
        log.info("=== searchSimple() - Recherche bachelier avec numéro table: {} ===", numeroTable);
        try {
            Query query = new Query(Criteria.where("numeroTable").is(numeroTable))
                    .collation(Collation.of("fr").strength(Collation.ComparisonLevel.primary()));

            NouveauBachelier nouveauBachelier = mongoTemplate.findOne(query, NouveauBachelier.class);
            log.info("Résultat recherche: {}", nouveauBachelier != null ? "trouvé" : "non trouvé");

            return Optional.ofNullable(nouveauBachelier).map(mapper::entiteToResponse);
        } catch (IllegalArgumentException e) {
            log.error("ID de série ou d'année invalide", e);
            throw new BusinessResourceException("ID de série ou d'année invalide", e);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche du NouveauBachelier", e);
            throw new BusinessResourceException("Erreur lors de la recherche du NouveauBachelier", e);
        }
    }

    @Override
    public List<String> importerDepuisCsv(InputStream inputStream) throws IOException {
        log.info("=== importerDepuisCsv() - Début import CSV ===");
        List<String> logs = new ArrayList<>();
        int ajoutCount = 0;
        int updateCount = 0;
        List<NouveauBachelier> nouveauxBacheliers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(';')
                     .withIgnoreEmptyLines()
                     .withTrim()
                     .withFirstRecordAsHeader())) {

            log.info("CSV parsé avec succès, traitement des lignes...");

            for (CSVRecord record : csvParser) {
                String numeroTable = record.get(3);
                log.debug("Traitement ligne {}: numéro table = {}", record.getRecordNumber(), numeroTable);

                if (numeroTable == null || numeroTable.isEmpty()) {
                    logs.add("⚠️ Ligne " + record.getRecordNumber() + ": Numéro de table vide. Ignoré.");
                    continue;
                }

                NouveauBachelier existing = mongoTemplate.findOne(
                        Query.query(Criteria.where("numeroTable").is(numeroTable)),
                        NouveauBachelier.class
                );

                if (existing != null) {
                    existing.setTelephone(record.get(0).replaceAll("\\s+", ""));
                    existing.setPrenoms(record.get(1));
                    existing.setNom(record.get(2));
                    existing.setResultat(record.get(4));
                    existing.setMention(record.get(5));

                    mongoTemplate.save(existing);
                    updateCount++;
                    logs.add("♻️ Mise à jour ligne " + record.getRecordNumber() + " [Table: " + numeroTable + "]");
                    log.debug("Mise à jour effectuée pour le bachelier: {}", numeroTable);
                } else {
                    NouveauBachelier nouveau = new NouveauBachelier();
                    nouveau.setNumeroTable(numeroTable);
                    nouveau.setTelephone(record.get(0).replaceAll("\\s+", ""));
                    nouveau.setPrenoms(record.get(1));
                    nouveau.setNom(record.get(2));
                    nouveau.setResultat(record.get(4));
                    nouveau.setMention(record.get(5));

                    nouveauxBacheliers.add(nouveau);
                    logs.add("✅ Ajout prévu ligne " + record.getRecordNumber() + " [Table: " + numeroTable + "]");
                    log.debug("Nouveau bachelier à ajouter: {}", numeroTable);
                }
            }

            if (!nouveauxBacheliers.isEmpty()) {
                log.info("Insertion batch de {} nouveaux bacheliers", nouveauxBacheliers.size());
                mongoTemplate.insert(nouveauxBacheliers, NouveauBachelier.class);
                ajoutCount = nouveauxBacheliers.size();
            }

            logs.add("=======================================");
            logs.add("Total lignes ajoutées (batch) : " + ajoutCount);
            logs.add("Total lignes mises à jour      : " + updateCount);
            log.info("=== importerDepuisCsv() - Fin. Ajoutés: {}, Mis à jour: {} ===", ajoutCount, updateCount);

        } catch (Exception e) {
            log.error("Erreur lors de l'import CSV: {}", e.getMessage(), e);
            logs.add("❌ Erreur : " + e.getMessage());
            throw e;
        }
        return logs;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                } else {
                    return String.valueOf(num);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
            case ERROR:
            default:
                return "";
        }
    }

    @Override
    public Optional<NouveauBachelierAudit> auditOneById(String id) throws NumberFormatException, BusinessResourceException {
        log.info("=== auditOneById() - Audit bachelier avec id: {} ===", id);
        try {
            NouveauBachelier oneBrute = dao.findById(id)
                    .orElseThrow(() -> new BusinessResourceException("not-found", "Aucune NouveauBachelier avec " + id + " trouvé.", HttpStatus.NOT_FOUND));

            log.info("Bachelier avec id: {} trouvé pour audit", id);
            Optional<NouveauBachelierAudit> response = Optional.ofNullable(mapper.toEntiteAudit(oneBrute, Long.valueOf("1"), Long.valueOf("1")));
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramètre id {} non autorisé. <auditOneById>.", id);
            throw new BusinessResourceException("not-valid-param", "Paramètre " + id + " non autorisé.", HttpStatus.BAD_REQUEST);
        }
    }

    // ── Parser le fichier Excel ──────────────────────────────────────────────────
    private List<BulkImportRow> parseExcel(InputStream inputStream) throws IOException {
        log.info("=== parseExcel() - Début parsing du fichier Excel ===");
        List<BulkImportRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            log.info("Workbook chargé avec succès");
            Sheet sheet = workbook.getSheetAt(0);
            log.info("Sheet récupérée: {}, nombre de lignes: {}", sheet.getSheetName(), sheet.getPhysicalNumberOfRows());

            int rowCount = 0;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    log.info("En-tête détectée, ignorée");
                    continue;
                }

                String numeroTable = getCellValue(row.getCell(3));
                log.debug("Ligne {}: numéro table = {}", row.getRowNum() + 1, numeroTable);

                if (numeroTable == null || numeroTable.isBlank()) {
                    log.warn("Ligne {}: numéro table vide, ignorée", row.getRowNum() + 1);
                    continue;
                }

                String juryCode = getCellValue(row.getCell(6));
                log.debug("Ligne {}: juryCode = {}", row.getRowNum() + 1, juryCode);

                rows.add(BulkImportRow.builder()
                        .telephone(getCellValue(row.getCell(0)).replaceAll("\\s+", ""))
                        .prenoms(getCellValue(row.getCell(1)))
                        .nom(getCellValue(row.getCell(2)))
                        .numeroTable(numeroTable)
                        .resultat(getCellValue(row.getCell(4)))
                        .mention(getCellValue(row.getCell(5)))
                        .juryCode(juryCode)
                        .rowNum(row.getRowNum() + 1)
                        .build());
                rowCount++;
            }
            log.info("parseExcel() - Fin. {} lignes valides parsées", rowCount);
        } catch (Exception e) {
            log.error("Erreur lors du parsing Excel: {}", e.getMessage(), e);
            throw new IOException("Erreur de parsing Excel: " + e.getMessage(), e);
        }
        return rows;
    }

    @Override
    public ImportResult importerDepuisExcel(InputStream inputStream) throws IOException {
        log.info("=== importerDepuisExcel() - DÉBUT IMPORT EXCEL ===");
        log.info("Timestamp début: {}", LocalDateTime.now());

        try {
            log.info("Étape 1: Parsing du fichier Excel...");
            List<BulkImportRow> rows = parseExcel(inputStream);
            log.info("Parsing terminé. {} lignes extraites du fichier", rows.size());

            if (rows.isEmpty()) {
                log.warn("Aucune donnée valide trouvée dans le fichier Excel");
                return ImportResult.vide();
            }

            log.info("Étape 2: Exécution de l'upsert batch...");
            ImportResult result = executerUpsertBatch(rows);

            log.info("=== importerDepuisExcel() - FIN IMPORT EXCEL ===");
            log.info("Résumé: Total={}, Nouveaux={}, Modifiés={}, Inchangés={}, JuryIntrouvable={}",
                    result.getTotal(), result.getNouveaux(), result.getModifies(),
                    result.getInchanges(), result.getJuryIntrouvable());
            log.info("Timestamp fin: {}", LocalDateTime.now());

            return result;
        } catch (Exception e) {
            log.error("ERREUR CRITIQUE lors de l'import Excel: {}", e.getMessage(), e);
            throw new IOException("Erreur lors de l'import Excel: " + e.getMessage(), e);
        }
    }

    private ImportResult executerUpsertBatch(List<BulkImportRow> rows) {
        log.info("=== executerUpsertBatch() - Début traitement batch ===");
        log.info("Nombre de lignes à traiter: {}", rows.size());

        // ── 1. Récupérer les numéros de jury uniques du fichier
        Set<String> juryNumeros = rows.stream()
                .map(BulkImportRow::getJuryCode)
                .filter(j -> j != null && !j.isBlank())
                .collect(Collectors.toSet());
        log.info("Numéros de jury uniques trouvés: {}", juryNumeros.size());
        log.info("Numéros recherchés: {}", juryNumeros);

        // ── 2. Charger tous les jurys en UNE seule requête MongoDB
        //    CORRECTION : Utiliser "numero" au lieu de "code" !
        log.info("Recherche des jurys dans MongoDB avec le champ 'numero'...");
        Map<String, Jury> jurysParNumero = mongoTemplate
                .find(Query.query(Criteria.where("numero").in(juryNumeros)), Jury.class)
                .stream()
                .collect(Collectors.toMap(Jury::getNumero, j -> j));  // ← Utiliser getNumero()

        log.info("Jurys trouvés: {} sur {} demandés", jurysParNumero.size(), juryNumeros.size());

        if (juryNumeros.size() != jurysParNumero.size()) {
            Set<String> nonTrouves = new HashSet<>(juryNumeros);
            nonTrouves.removeAll(jurysParNumero.keySet());
            log.warn("Jurys non trouvés: {}", nonTrouves);

            // Afficher les jurys existants pour debug
            List<Jury> tousLesJurys = mongoTemplate.findAll(Jury.class);
            log.info("Premiers jurys existants (pour debug):");
            tousLesJurys.stream().limit(10).forEach(j ->
                    log.info("  - numero: {}, name: {}", j.getNumero(), j.getName())
            );
        }

        // ── 3. Récupérer les bacheliers existants
        List<String> numeros = rows.stream()
                .map(BulkImportRow::getNumeroTable)
                .collect(Collectors.toList());
        log.info("Recherche des bacheliers existants...");

        Map<String, NouveauBachelier> existants = mongoTemplate
                .find(Query.query(Criteria.where("numeroTable").in(numeros)), NouveauBachelier.class)
                .stream()
                .collect(Collectors.toMap(NouveauBachelier::getNumeroTable, b -> b));
        log.info("Bacheliers existants trouvés: {}", existants.size());

        // ── 4. Préparer le BulkOps
        BulkOperations bulkOps = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED, NouveauBachelier.class
        );

        int nouveaux = 0, modifies = 0, inchanges = 0, juryIntrouvable = 0;
        List<String> warnings = new ArrayList<>();
        LocalDateTime maintenant = LocalDateTime.now();

        for (BulkImportRow row : rows) {
            // ── 5. Résoudre le Jury depuis la Map
            Jury jury = jurysParNumero.get(row.getJuryCode());
            if (jury == null) {
                String warning = String.format("⚠️ Ligne %d [Table: %s] — Jury introuvable: %s",
                        row.getRowNum(), row.getNumeroTable(), row.getJuryCode());
                warnings.add(warning);
                log.warn(warning);
                juryIntrouvable++;
                continue;
            }

            NouveauBachelier existant = existants.get(row.getNumeroTable());
            String nouveauHash = computeHash(row);

            // ── 6. Sauter si rien n'a changé
            if (existant != null && nouveauHash.equals(existant.getHashResultat())) {
                log.debug("Ligne {}: aucun changement pour le bachelier {}", row.getRowNum(), row.getNumeroTable());
                inchanges++;
                continue;
            }

            Query query = Query.query(Criteria.where("numeroTable").is(row.getNumeroTable()));
            Update update = new Update()
                    .set("telephone", row.getTelephone())
                    .set("prenoms", row.getPrenoms())
                    .set("nom", row.getNom())
                    .set("resultat", row.getResultat())
                    .set("mention", row.getMention())
                    .set("jury", jury)
                    .set("hashResultat", nouveauHash)
                    .set("dateImport", maintenant)
                    .set("dateModification", maintenant)
                    .setOnInsert("numeroTable", row.getNumeroTable())
                    .setOnInsert("dateCreation", maintenant);

            bulkOps.upsert(query, update);

            if (existant == null) {
                nouveaux++;
                log.debug("Ligne {}: nouveau bachelier à insérer: {}", row.getRowNum(), row.getNumeroTable());
            } else {
                modifies++;
                log.debug("Ligne {}: mise à jour du bachelier: {}", row.getRowNum(), row.getNumeroTable());
            }
        }

        // ── 7. Exécuter le batch
        if (nouveaux + modifies > 0) {
            log.info("Exécution du bulk upsert: {} nouvelles, {} modifications", nouveaux, modifies);
            try {
                bulkOps.execute();
                log.info("Bulk upsert exécuté avec succès");
            } catch (Exception e) {
                log.error("Erreur lors de l'exécution du bulk upsert: {}", e.getMessage(), e);
                throw e;
            }
        } else {
            log.info("Aucune opération d'upsert à exécuter");
        }

        ImportResult result = ImportResult.builder()
                .nouveaux(nouveaux)
                .modifies(modifies)
                .inchanges(inchanges)
                .juryIntrouvable(juryIntrouvable)
                .warnings(warnings)
                .total(rows.size())
                .build();

        log.info("=== executerUpsertBatch() - Fin ===");
        log.info("Résultat: Nouveaux={}, Modifiés={}, Inchangés={}, JuryIntrouvable={}, Total={}",
                nouveaux, modifies, inchanges, juryIntrouvable, rows.size());

        return result;
    }

    private String computeHash(BulkImportRow row) {
        String data = row.getResultat() + "|" + row.getMention() + "|"
                + row.getNom() + "|" + row.getPrenoms() + "|"
                + row.getTelephone() + "|" + row.getJuryCode();
        String hash = Integer.toHexString(data.hashCode());
        log.debug("Hash calculé pour {}: {}", row.getNumeroTable(), hash);
        return hash;
    }
    @Override
    public Optional<NouveauBachelierResponse> getBachelierByNumeroTable(String numeroTable) throws BusinessResourceException {
        log.info("=== getBachelierByNumeroTable() - Recherche bachelier avec numéro table: {} ===", numeroTable);

        try {
            // Nettoyer le numéro de table (enlever espaces)
            String cleanedNumeroTable = numeroTable.trim().replaceAll("\\s+", "");

            // Recherche exacte
            Query query = Query.query(Criteria.where("numeroTable").is(cleanedNumeroTable));

            NouveauBachelier bachelier = mongoTemplate.findOne(query, NouveauBachelier.class);

            if (bachelier == null) {
                log.warn("Aucun bachelier trouvé avec le numéro table: {}", cleanedNumeroTable);
                return Optional.empty();
            }
            NouveauBachelierResponse response = mapper.entiteToResponse(bachelier);

            // Si le résultat n'est pas "Admis(e)", on ne retourne pas la mention
            String resultat = bachelier.getResultat();
            if (resultat == null || (!resultat.contains("Admis") && !resultat.contains("ADMIS"))) {
                response.setMention(null);
            }
            log.info("Bachelier trouvé: {} {} - Résultat: {}, Mention: {}",
                    bachelier.getPrenoms(), bachelier.getNom(),
                    bachelier.getResultat(),
                    (response.getMention() != null ? response.getMention() : "non retournée"));

            return Optional.of(response);

        } catch (Exception e) {
            log.error("Erreur lors de la recherche du bachelier: {}", e.getMessage(), e);
            throw new BusinessResourceException("search-error", "Erreur lors de la recherche", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}