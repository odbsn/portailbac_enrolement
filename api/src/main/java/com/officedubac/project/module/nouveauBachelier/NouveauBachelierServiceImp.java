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
    private  final MongoTemplate mongoTemplate;
    private final NouveauBachelierDao dao;
    @Override
    public List<NouveauBachelierResponse> all() throws BusinessResourceException {
        log.info("NouveauBachelierServiceImp::all");
        List<NouveauBachelier> all = dao.findAll();
        List<NouveauBachelierResponse> response;
        response = all.stream()
                .map(mapper::entiteToResponse)
                .collect(Collectors.toList());
        return response;
    }



    @Override
    public Optional<NouveauBachelierResponse> oneById(String id) throws NumberFormatException, BusinessResourceException {
        try {
//            String decryptedId = encryptionUtil.decrypt(id);

            // Maintenant que vous avez l'ID en texte clair, effectuez la recherche dans la base de données
            NouveauBachelier nouveauBachelier = mongoTemplate.findById(id, NouveauBachelier.class);
            log.info("Bacherlier  avec id: " + id + " trouvé. <oneByIdNouveauBachelier>");
            Optional<NouveauBachelierResponse> response;
            response = Optional.ofNullable(mapper.entiteToResponse(nouveauBachelier));
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramétre id " + id + " non autorisé. <oneById>.");
            throw new BusinessResourceException("not-valid-param", "Paramétre " + id + " non autorisé.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public NouveauBachelierResponse add(NouveauBachelierRequest req) throws BusinessResourceException {
        try {
            log.info("Debug 001-add:  " + req.toString());
            NouveauBachelier one = mapper.requestToEntity(req);
            log.info("Debug 001-req_to_entity:  " + one.toString());
            NouveauBachelierResponse response = mapper.entiteToResponse(dao.save(one));
            log.info("Ajout " + response.getNumeroTable()+ " effectué avec succés. <add>");
            return response;
        } catch (ResourceAlreadyExists | DataIntegrityViolationException e) {
            log.error("Erreur technique de creation NouveauNouveauBachelier: donnée en doublon ou contrainte non respectée" + e.toString());
            throw new BusinessResourceException("data-error", "Donnée en doublon ou contrainte non respectée ", HttpStatus.CONFLICT);
        } catch (Exception ex) {
            log.error("Ajout NouveauNouveauBachelier: Une erreur inattandue est rencontrée." + ex.getMessage());
            throw new BusinessResourceException("technical-error", "Erreur technique de création d'un NouveauNouveauBachelier: " + req.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public NouveauBachelierResponse maj(NouveauBachelierRequest req, String id) throws NumberFormatException, NoSuchElementException, BusinessResourceException {
        try {
            NouveauBachelier NouveauBachelierOptional;
            NouveauBachelierOptional = dao.findById(id)
                    .orElseThrow(
                            () -> new BusinessResourceException("not-found", "Aucun NouveauNouveauBachelier avec " + id + " trouvé.", HttpStatus.NOT_FOUND)
                    );
            //NouveauNouveauBachelier NouveauBachelier = mapper.anneRequestToNouveauBachelierUp(NouveauBachelierOptional, req, userService.user());
            NouveauBachelier oneBrute = mapper.requestToEntiteUp(NouveauBachelierOptional, req);
            NouveauBachelierResponse response = mapper.entiteToResponse(dao.save(oneBrute));
            log.info("Mise à jour " + response.getNumeroTable() + " effectuée avec succés. <maj>");
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramétre id " + id + " non autorisé. <maj>.");
            throw new BusinessResourceException("not-valid-param", "Paramétre " + id + " non autorisé.", HttpStatus.BAD_REQUEST);
        } catch (ResourceAlreadyExists | DataIntegrityViolationException e) {
            log.error("Erreur technique de maj NouveauNouveauBachelier: donnée en doublon ou contrainte non respectée" + e.toString());
            throw new BusinessResourceException("data-error", "Donnée en doublon ou contrainte non respectée ", HttpStatus.CONFLICT);
        } catch (Exception ex) {
            log.error("Maj imputation: Une erreur inattandue est rencontrée." + ex.toString());
            throw new BusinessResourceException("technical-error", "Erreur technique de mise à jour d'un NouveauNouveauBachelier: " + req.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    @Transactional(readOnly = false)
    public String del(String id) throws NumberFormatException, BusinessResourceException {
        try {
            NouveauBachelier oneBrute = dao.findById(id)
                    .orElseThrow(
                            () -> new BusinessResourceException("not-found", "Aucun NouveauNouveauBachelier avec " + id + " trouvé.", HttpStatus.NOT_FOUND)
                    );
            dao.deleteById(id);
            log.info("NouveauBachelier avec id & : " + id + " & " + oneBrute.getNumeroTable() + " supprimé avec succés. <del>");
            String response;
            response = "Imputation: " + oneBrute.getNumeroTable() + " supprimé avec succés. <del>";
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramétre id " + id + " non autorisé. <del>.");
            throw new BusinessResourceException("not-valid-param", "Paramétre " + id + " non autorisé.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Optional<NouveauBachelierResponse> searchSimple(String numeroTable) throws BusinessResourceException {
        try {
            // Création de la requête avec critère et collation
            Query query = new Query(Criteria.where("numeroTable").is(numeroTable))
                    .collation(Collation.of("fr").strength(Collation.ComparisonLevel.primary()));

            // Log des paramètres de recherche
            log.info("Recherche avec le paramètre : numeroTable={}", numeroTable);

            // Exécution de la requête
            NouveauBachelier nouveauBachelier = mongoTemplate.findOne(query, NouveauBachelier.class);
            log.info("Résultats trouvés : {}", nouveauBachelier);

            // Conversion de l'entité en réponse
            return Optional.ofNullable(nouveauBachelier)
                    .map(mapper::entiteToResponse);

        } catch (IllegalArgumentException e) {
            throw new BusinessResourceException("ID de série ou d'année invalide", e);
        } catch (Exception e) {
            throw new BusinessResourceException("Erreur lors de la recherche du NouveauBachelier", e);
        }
    }
//    @Override
//    public List<String> importerDepuisExcel(InputStream inputStream) throws IOException {
//        List<String> logs = new ArrayList<>();
//        int ajoutCount = 0;
//        int updateCount = 0;
//
//        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; // Ignorer l'en-tête
//
//                String numeroTable = getCellValue(row.getCell(3));
//                if (numeroTable == null || numeroTable.isEmpty()) {
//                    logs.add("⚠️ Ligne " + (row.getRowNum() + 1) + ": Numéro de table vide. Ignoré.");
//                    continue;
//                }
//
//                NouveauBachelier bachelier = mongoTemplate.findOne(
//                        Query.query(Criteria.where("numeroTable").is(numeroTable)),
//                        NouveauBachelier.class
//                );
//
//                boolean isNew = (bachelier == null);
//                if (isNew) {
//                    bachelier = new NouveauBachelier();
//                    bachelier.setNumeroTable(numeroTable);
//                    ajoutCount++;
//                } else {
//                    updateCount++;
//                }
//
//                // Mise à jour des champs
//                bachelier.setTelephone(getCellValue(row.getCell(0)).replaceAll("\\s+", ""));
//                bachelier.setPrenoms(getCellValue(row.getCell(1)));
//                bachelier.setNom(getCellValue(row.getCell(2)));
//                bachelier.setResultat(getCellValue(row.getCell(4)));
//                bachelier.setMention(getCellValue(row.getCell(5)));
//
//                mongoTemplate.save(bachelier);
//
//                logs.add((isNew ? "✅ Ajout" : "♻️ Mise à jour")
//                        + " ligne " + (row.getRowNum() + 1)
//                        + " [Table: " + numeroTable + "]");
//            }
//            logs.add("=======================================");
//            logs.add("Total lignes ajoutées : " + ajoutCount);
//            logs.add("Total lignes mises à jour : " + updateCount);
//
//        } catch (Exception e) {
//            logs.add("❌ Erreur : " + e.getMessage());
//            e.printStackTrace();
//        }
//        return logs;
//    }

    @Override
    public List<String> importerDepuisCsv(InputStream inputStream) throws IOException {
        List<String> logs = new ArrayList<>();
        int ajoutCount = 0;
        int updateCount = 0;

        List<NouveauBachelier> nouveauxBacheliers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(';')                 // Séparateur FR/Excel
                     .withIgnoreEmptyLines()            // Ignore les lignes vides
                     .withTrim()                        // Nettoie les espaces
                     .withFirstRecordAsHeader()
        )) {

            for (CSVRecord record : csvParser) {
                String numeroTable = record.get(3); // 4ᵉ colonne

                if (numeroTable == null || numeroTable.isEmpty()) {
                    logs.add("⚠️ Ligne " + record.getRecordNumber() + ": Numéro de table vide. Ignoré.");
                    continue;
                }

                NouveauBachelier existing = mongoTemplate.findOne(
                        Query.query(Criteria.where("numeroTable").is(numeroTable)),
                        NouveauBachelier.class
                );

                if (existing != null) {
                    // Mise à jour si déjà existant
                    existing.setTelephone(record.get(0).replaceAll("\\s+", ""));
                    existing.setPrenoms(record.get(1));
                    existing.setNom(record.get(2));
                    existing.setResultat(record.get(4));
                    existing.setMention(record.get(5));

                    mongoTemplate.save(existing); // sauvegarde individuelle
                    updateCount++;
                    logs.add("♻️ Mise à jour ligne " + record.getRecordNumber() + " [Table: " + numeroTable + "]");
                } else {
                    // Nouveau bachelier à insérer en lot
                    NouveauBachelier nouveau = new NouveauBachelier();
                    nouveau.setNumeroTable(numeroTable);
                    nouveau.setTelephone(record.get(0).replaceAll("\\s+", ""));
                    nouveau.setPrenoms(record.get(1));
                    nouveau.setNom(record.get(2));
                    nouveau.setResultat(record.get(4));
                    nouveau.setMention(record.get(5));

                    nouveauxBacheliers.add(nouveau);
                    logs.add("✅ Ajout prévu ligne " + record.getRecordNumber() + " [Table: " + numeroTable + "]");
                }
            }

            if (!nouveauxBacheliers.isEmpty()) {
                mongoTemplate.insert(nouveauxBacheliers, NouveauBachelier.class);
                ajoutCount = nouveauxBacheliers.size();
            }

            logs.add("=======================================");
            logs.add("Total lignes ajoutées (batch) : " + ajoutCount);
            logs.add("Total lignes mises à jour      : " + updateCount);

        } catch (Exception e) {
            logs.add("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
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
        try {
            NouveauBachelier oneBrute = dao.findById(id)
                    .orElseThrow(
                            () -> new BusinessResourceException("not-found", "Aucune NouveauNouveauBachelier avec " + id + " trouvé.", HttpStatus.NOT_FOUND)
                    );
            log.info("NouveauNouveauBachelier avec id: " + id + " trouvé. <auditOneById>");
           Optional<NouveauBachelierAudit> response;
            response = Optional.ofNullable(mapper.toEntiteAudit(oneBrute, Long.valueOf("1"), Long.valueOf("1") ));
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramétre id " + id + " non autorisé. <auditOneById>.");
            throw new BusinessResourceException("not-valid-param", "Paramétre " + id + " non autorisé.", HttpStatus.BAD_REQUEST);
        }

    }
    // ── Parser le fichier Excel ──────────────────────────────────────────────────
    private List<BulkImportRow> parseExcel(InputStream inputStream) throws IOException {
        List<BulkImportRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip en-tête

                String numeroTable = getCellValue(row.getCell(3));
                if (numeroTable == null || numeroTable.isBlank()) continue;

                // 🆕 Jury lu depuis la colonne 6 (index 6)
                String juryCode = getCellValue(row.getCell(6));

                rows.add(BulkImportRow.builder()
                        .telephone(getCellValue(row.getCell(0)).replaceAll("\\s+", ""))
                        .prenoms(getCellValue(row.getCell(1)))
                        .nom(getCellValue(row.getCell(2)))
                        .numeroTable(numeroTable)
                        .resultat(getCellValue(row.getCell(4)))
                        .mention(getCellValue(row.getCell(5)))
                        .juryCode(juryCode)   // 🆕 depuis la ligne
                        .rowNum(row.getRowNum() + 1)
                        .build());
            }
        }
        return rows;
    }
    @Override
    public ImportResult importerDepuisExcel(InputStream inputStream) throws IOException {

        // 🆕 Plus de extraction du jury depuis nomFichier
        //     Le jury est lu directement dans chaque ligne Excel
        List<BulkImportRow> rows = parseExcel(inputStream);

        if (rows.isEmpty()) {
            return ImportResult.vide();
        }

        return executerUpsertBatch(rows);
    }
    private ImportResult executerUpsertBatch(List<BulkImportRow> rows) {

        // ── 1. Récupérer les numéros de jury uniques du fichier
        Set<String> juryNumeros = rows.stream()
                .map(BulkImportRow::getJuryCode)
                .filter(j -> j != null && !j.isBlank())
                .collect(Collectors.toSet());

        // ── 2. Charger tous les jurys en UNE seule requête MongoDB
        Map<String, Jury> jurysParNumero = mongoTemplate
                .find(Query.query(Criteria.where("numero").in(juryNumeros)), Jury.class)
                .stream()
                .collect(Collectors.toMap(Jury::getCode, j -> j));

        // ── 3. Récupérer les bacheliers existants en UNE seule requête
        List<String> numeros = rows.stream()
                .map(BulkImportRow::getNumeroTable)
                .collect(Collectors.toList());

        Map<String, NouveauBachelier> existants = mongoTemplate
                .find(Query.query(Criteria.where("numeroTable").in(numeros)), NouveauBachelier.class)
                .stream()
                .collect(Collectors.toMap(NouveauBachelier::getNumeroTable, b -> b));

        // ── 4. Préparer le BulkOps
        BulkOperations bulkOps = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED, NouveauBachelier.class
        );

        int nouveaux = 0, modifies = 0, inchanges = 0, juryIntrouvable = 0;
        List<String> warnings = new ArrayList<>();
        LocalDateTime maintenant = LocalDateTime.now();

        for (BulkImportRow row : rows) {

            // ── 5. Résoudre le Jury depuis la Map (pas de requête supplémentaire)
            Jury jury = jurysParNumero.get(row.getJuryCode());
            if (jury == null) {
                warnings.add("⚠️ Ligne " + row.getRowNum()
                        + " [Table: " + row.getNumeroTable()
                        + "] — Jury introuvable: " + row.getJuryCode());
                juryIntrouvable++;
                continue;
            }
            NouveauBachelier existant = existants.get(row.getNumeroTable());
            String nouveauHash = computeHash(row);

            // ── 6. Sauter si rien n'a changé
            if (existant != null && nouveauHash.equals(existant.getHashResultat())) {
                inchanges++;
                continue;
            }
            Query query = Query.query(
                    Criteria.where("numeroTable").is(row.getNumeroTable())
            );

            Update update = new Update()
                    .set("telephone",      row.getTelephone())
                    .set("prenoms",        row.getPrenoms())
                    .set("nom",            row.getNom())
                    .set("resultat",       row.getResultat())
                    .set("mention",        row.getMention())
                    .set("jury",           jury)          // ✅ objet Jury complet embarqué
                    .set("hashResultat",   nouveauHash)
                    .set("dateImport",     maintenant)
                    .set("dateModification", maintenant)
                    .setOnInsert("numeroTable",  row.getNumeroTable())
                    .setOnInsert("dateCreation", maintenant);

            bulkOps.upsert(query, update);

            if (existant == null) nouveaux++;
            else modifies++;
        }

        // ── 7. Exécuter le batch
        if (nouveaux + modifies > 0) {
            bulkOps.execute();
        }

        return ImportResult.builder()
                .nouveaux(nouveaux)
                .modifies(modifies)
                .inchanges(inchanges)
                .juryIntrouvable(juryIntrouvable)
                .warnings(warnings)
                .total(rows.size())
                .build();
    }
    private String computeHash(BulkImportRow row) {
        String data = row.getResultat() + "|" + row.getMention()  + "|"
                + row.getNom()      + "|" + row.getPrenoms()  + "|"
                + row.getTelephone()+ "|" + row.getJuryCode(); // 🆕
        return Integer.toHexString(data.hashCode());
    }


}
