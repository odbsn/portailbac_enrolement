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
import org.bson.Document;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
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
                log.warn("Résultat non encore disponible pour le numéro table: {}", cleanedNumeroTable);
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
    // ════════════════════════════════════════════════════════════════════════
    // ✅ PAGINATION SERVEUR (remplace all() pour les gros volumes — 180k+ lignes)
    // ════════════════════════════════════════════════════════════════════════
    @Override
    public Page<NouveauBachelierResponse> findAllPaginated(Pageable pageable, String search) throws BusinessResourceException {
        log.info("=== findAllPaginated() - page={}, size={}, search='{}' ===",
                pageable.getPageNumber(), pageable.getPageSize(), search);

        Query query = new Query();

        if (search != null && !search.isBlank()) {
            String regex = Pattern.quote(search.trim());
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("nom").regex(regex, "i"),
                    Criteria.where("prenoms").regex(regex, "i"),
                    Criteria.where("numeroTable").regex(regex, "i"),
                    Criteria.where("telephone").regex(regex, "i"),
                    Criteria.where("jury.numero").regex(regex, "i")
            );
            query.addCriteria(searchCriteria);
        }

        // ── Total (avant pagination) ──
        long total = mongoTemplate.count(query, NouveauBachelier.class);

        // ── Tri + pagination ──
        query.with(pageable);

        List<NouveauBachelier> bacheliers = mongoTemplate.find(query, NouveauBachelier.class);

        List<NouveauBachelierResponse> content = bacheliers.stream()
                .map(mapper::entiteToResponse)
                .collect(Collectors.toList());

        log.info("findAllPaginated() - {} résultats sur {} total", content.size(), total);

        return new PageImpl<>(content, pageable, total);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ✅ JURYS NON ENCORE CHARGÉS DANS nouveauBachelier
    // ════════════════════════════════════════════════════════════════════════
    @Override
    public List<Jury> findJurysNonCharges(Boolean technique) throws BusinessResourceException {
        log.info("=== findJurysNonCharges() - technique={} ===", technique);
        try {
            // 1. Jurys existants, filtrés par "technique" si précisé
            Query juryQuery = new Query();
            if (technique != null) {
                juryQuery.addCriteria(Criteria.where("technique").is(technique));
            }
            List<Jury> tousLesJurys = mongoTemplate.find(juryQuery, Jury.class);
            log.info("Total jurys en base (technique={}): {}", technique, tousLesJurys.size());

            // 2. Numéros de jury déjà présents dans nouveauBachelier (distinct)
            Set<String> numerosCharges = mongoTemplate
                    .findDistinct("jury.numero", NouveauBachelier.class, String.class)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            log.info("Numéros de jury déjà chargés dans nouveauBachelier: {}", numerosCharges.size());

            // 3. Jurys dont le numero n'apparaît pas dans la liste chargée
            List<Jury> jurysNonCharges = tousLesJurys.stream()
                    .filter(j -> j.getNumero() == null || !numerosCharges.contains(j.getNumero()))
                    .collect(Collectors.toList());

            log.info("=== findJurysNonCharges() - {} jury(s) sans bachelier chargé (technique={}) ===",
                    jurysNonCharges.size(), technique);
            return jurysNonCharges;
        } catch (Exception e) {
            log.error("Erreur lors de la recherche des jurys non chargés: {}", e.getMessage(), e);
            throw new BusinessResourceException("technical-error",
                    "Erreur lors de la recherche des jurys non chargés", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // ════════════════════════════════════════════════════════════════════════
// ✅ MODIFICATIONS À APPORTER À NouveauBachelierServiceImp.java
// ════════════════════════════════════════════════════════════════════════

// ── 1. AJOUTER CES IMPORTS ────────────────────────────────────────────────
//
// import org.springframework.web.multipart.MultipartFile;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;


// ── 2. REMPLACER parseExcel(InputStream) PAR CETTE VERSION ───────────────
//    (ajoute le nom du fichier source sur chaque ligne parsée)

    private List<BulkImportRow> parseExcel(InputStream inputStream, String nomFichier) throws IOException {
        log.info("=== parseExcel() - Début parsing du fichier Excel: {} ===", nomFichier);
        List<BulkImportRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            log.info("Workbook chargé avec succès: {}", nomFichier);
            Sheet sheet = workbook.getSheetAt(0);
            log.info("[{}] Sheet récupérée: {}, nombre de lignes: {}",
                    nomFichier, sheet.getSheetName(), sheet.getPhysicalNumberOfRows());

            int rowCount = 0;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // en-tête
                }

                String numeroTable = getCellValue(row.getCell(4));

                if (numeroTable == null || numeroTable.isBlank()) {
                    log.warn("[{}] Ligne {}: numéro table vide, ignorée", nomFichier, row.getRowNum() + 1);
                    continue;
                }

                String juryCode = getCellValue(row.getCell(3));

                rows.add(BulkImportRow.builder()
                        .telephone(getCellValue(row.getCell(0)).replaceAll("\\s+", ""))
                        .prenoms(getCellValue(row.getCell(1)))
                        .nom(getCellValue(row.getCell(2)))
                        .numeroTable(numeroTable)
                        .resultat(getCellValue(row.getCell(5)))
                        .mention(getCellValue(row.getCell(6)))
                        .juryCode(juryCode)
                        .rowNum(row.getRowNum() + 1)
                        .fichierSource(nomFichier)
                        .build());
                rowCount++;
            }
            log.info("[{}] parseExcel() - Fin. {} lignes valides parsées", nomFichier, rowCount);
        } catch (Exception e) {
            log.error("[{}] Erreur lors du parsing Excel: {}", nomFichier, e.getMessage(), e);
            throw new IOException("Erreur de parsing Excel [" + nomFichier + "]: " + e.getMessage(), e);
        }
        return rows;
    }


// ── 3. importerDepuisExcel(InputStream) EXISTANT : à garder pour compat,
//       mais il doit maintenant passer un nom de fichier générique ────────

    @Override
    public ImportResult importerDepuisExcel(InputStream inputStream) throws IOException {
        log.info("=== importerDepuisExcel() - DÉBUT IMPORT EXCEL (fichier unique) ===");
        try {
            List<BulkImportRow> rows = parseExcel(inputStream, "fichier_unique");
            if (rows.isEmpty()) {
                return ImportResult.vide();
            }
            ImportResult result = executerUpsertBatch(rows).get("fichier_unique");
            log.info("=== importerDepuisExcel() - FIN ===");
            return result != null ? result : ImportResult.vide();
        } catch (Exception e) {
            log.error("ERREUR CRITIQUE lors de l'import Excel: {}", e.getMessage(), e);
            throw new IOException("Erreur lors de l'import Excel: " + e.getMessage(), e);
        }
    }


// ── 4. NOUVELLE MÉTHODE : import multi-fichiers, parsing PARALLÈLE,
//       UN SEUL batch MongoDB fusionné, résultats répartis par fichier ───
//       (à déclarer aussi dans l'interface NouveauBachelierService)

    @Override
    public List<ImportResult> importerDepuisExcelMultiple(List<MultipartFile> files) throws IOException {
        log.info("=== importerDepuisExcelMultiple() - DÉBUT - {} fichier(s) ===", files.size());
        long t0 = System.currentTimeMillis();

        // ── a. Parsing en parallèle : I/O + CPU indépendants par fichier ──
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(files.size(), Runtime.getRuntime().availableProcessors())
        );

        try {
            List<CompletableFuture<List<BulkImportRow>>> futures = files.stream()
                    .map(file -> CompletableFuture.supplyAsync(() -> {
                        String nomFichier = file.getOriginalFilename() != null
                                ? file.getOriginalFilename() : "fichier_" + System.nanoTime();
                        try (InputStream is = file.getInputStream()) {
                            return parseExcel(is, nomFichier);
                        } catch (IOException e) {
                            log.error("Erreur lecture fichier {}: {}", nomFichier, e.getMessage(), e);
                            // On retourne une liste vide pour ce fichier plutôt que de faire échouer tout le batch
                            return Collections.<BulkImportRow>emptyList();
                        }
                    }, executor))
                    .collect(Collectors.toList());

            // ── b. Attendre tous les parsings et fusionner toutes les lignes ──
            List<BulkImportRow> toutesLesLignes = futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            long tParsing = System.currentTimeMillis();
            log.info("Parsing parallèle terminé en {} ms — {} lignes au total sur {} fichier(s)",
                    (tParsing - t0), toutesLesLignes.size(), files.size());

            if (toutesLesLignes.isEmpty()) {
                log.warn("Aucune ligne valide extraite des fichiers fournis");
                return files.stream()
                        .map(f -> ImportResult.builder()
                                .fichier(f.getOriginalFilename())
                                .total(0).nouveaux(0).modifies(0).inchanges(0)
                                .juryIntrouvable(0).warnings(new ArrayList<>())
                                .build())
                        .collect(Collectors.toList());
            }

            // ── c. UN SEUL batch MongoDB fusionné (1 requête Jury, 1 bulkOps) ──
            Map<String, ImportResult> resultatsParFichier = executerUpsertBatch(toutesLesLignes);

            long tBatch = System.currentTimeMillis();
            log.info("Batch MongoDB fusionné terminé en {} ms (total: {} ms)",
                    (tBatch - tParsing), (tBatch - t0));

            // ── d. S'assurer qu'un résultat existe pour chaque fichier fourni
            //       (même les fichiers vides ou en erreur de lecture) ──────────
            List<ImportResult> resultatsFinaux = files.stream()
                    .map(f -> {
                        String nom = f.getOriginalFilename() != null ? f.getOriginalFilename() : "inconnu";
                        ImportResult r = resultatsParFichier.get(nom);
                        if (r == null) {
                            r = ImportResult.builder()
                                    .fichier(nom)
                                    .total(0).nouveaux(0).modifies(0).inchanges(0)
                                    .juryIntrouvable(0).warnings(new ArrayList<>())
                                    .build();
                        }
                        return r;
                    })
                    .collect(Collectors.toList());

            log.info("=== importerDepuisExcelMultiple() - FIN — {} ms total ===", (System.currentTimeMillis() - t0));
            resultatsFinaux.forEach(r -> log.info(r.toSummary()));

            return resultatsFinaux;
        } finally {
            executor.shutdown();
        }
    }


// ── 5. executerUpsertBatch : retourne maintenant une Map<fichierSource, ImportResult>
//       au lieu d'un ImportResult unique, pour permettre la répartition par fichier ─

    private Map<String, ImportResult> executerUpsertBatch(List<BulkImportRow> rows) {
        log.info("=== executerUpsertBatch() - Début traitement batch fusionné — {} lignes ===", rows.size());

        // ── 1. Récupérer les numéros de jury uniques (tous fichiers confondus) ──
        Set<String> juryNumeros = rows.stream()
                .map(BulkImportRow::getJuryCode)
                .filter(j -> j != null && !j.isBlank())
                .collect(Collectors.toSet());
        log.info("Numéros de jury uniques trouvés: {}", juryNumeros.size());

        // ── 2. UNE SEULE requête MongoDB pour tous les jurys ──
        Map<String, Jury> jurysParNumero = mongoTemplate
                .find(Query.query(Criteria.where("numero").in(juryNumeros)), Jury.class)
                .stream()
                .collect(Collectors.toMap(Jury::getNumero, j -> j));
        log.info("Jurys trouvés: {} sur {} demandés", jurysParNumero.size(), juryNumeros.size());

        // ── 3. Bacheliers existants (toutes lignes confondues) ──
        List<String> numeros = rows.stream()
                .map(BulkImportRow::getNumeroTable)
                .collect(Collectors.toList());

        Map<String, NouveauBachelier> existants = mongoTemplate
                .find(Query.query(Criteria.where("numeroTable").in(numeros)), NouveauBachelier.class)
                .stream()
                .collect(Collectors.toMap(NouveauBachelier::getNumeroTable, b -> b));
        log.info("Bacheliers existants trouvés: {}", existants.size());

        // ── 4. UN SEUL BulkOps pour toutes les lignes de tous les fichiers ──
        BulkOperations bulkOps = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED, NouveauBachelier.class
        );

        // Compteurs PAR FICHIER SOURCE
        Map<String, int[]> compteursParFichier = new HashMap<>(); // [nouveaux, modifies, inchanges, juryIntrouvable]
        Map<String, List<String>> warningsParFichier = new HashMap<>();
        Map<String, Integer> totalParFichier = new HashMap<>();

        LocalDateTime maintenant = LocalDateTime.now();
        int operationsEnAttente = 0;

        for (BulkImportRow row : rows) {
            String fichier = row.getFichierSource() != null ? row.getFichierSource() : "inconnu";
            int[] compteurs = compteursParFichier.computeIfAbsent(fichier, k -> new int[4]);
            totalParFichier.merge(fichier, 1, Integer::sum);

            // ── Résoudre le Jury ──
            Jury jury = jurysParNumero.get(row.getJuryCode());
            if (jury == null) {
                String warning = String.format("⚠️ Ligne %d [Table: %s] — Jury introuvable: %s",
                        row.getRowNum(), row.getNumeroTable(), row.getJuryCode());
                warningsParFichier.computeIfAbsent(fichier, k -> new ArrayList<>()).add(warning);
                compteurs[3]++; // juryIntrouvable
                continue;
            }

            NouveauBachelier existant = existants.get(row.getNumeroTable());
            String nouveauHash = computeHash(row);

            // ── Sauter si rien n'a changé ──
            if (existant != null && nouveauHash.equals(existant.getHashResultat())) {
                compteurs[2]++; // inchanges
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
            operationsEnAttente++;

            if (existant == null) {
                compteurs[0]++; // nouveaux
            } else {
                compteurs[1]++; // modifies
            }
        }

        // ── 5. Exécuter LE batch unique ──
        if (operationsEnAttente > 0) {
            log.info("Exécution du bulk upsert fusionné: {} opérations", operationsEnAttente);
            try {
                bulkOps.execute();
                log.info("Bulk upsert fusionné exécuté avec succès");
            } catch (Exception e) {
                log.error("Erreur lors de l'exécution du bulk upsert fusionné: {}", e.getMessage(), e);
                throw e;
            }
        } else {
            log.info("Aucune opération d'upsert à exécuter");
        }

        // ── 6. Construire un ImportResult par fichier ──
        Map<String, ImportResult> resultats = new HashMap<>();
        for (String fichier : totalParFichier.keySet()) {
            int[] c = compteursParFichier.get(fichier);
            resultats.put(fichier, ImportResult.builder()
                    .fichier(fichier)
                    .total(totalParFichier.get(fichier))
                    .nouveaux(c[0])
                    .modifies(c[1])
                    .inchanges(c[2])
                    .juryIntrouvable(c[3])
                    .warnings(warningsParFichier.getOrDefault(fichier, new ArrayList<>()))
                    .build());
        }

        log.info("=== executerUpsertBatch() - Fin — résultats répartis sur {} fichier(s) ===", resultats.size());
        return resultats;
    }
    @Override
    public List<Jury> findJuryNumerosAvec2emeGroupe() {
        log.info("=== findJuryNumerosAvec2emeGroupe() - Début ===");

        // 1. Numéros de jury distincts ayant au moins un résultat "2ème groupe"
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("resultat").is("Autorisé(s) au 2ème groupe d'épreuves")),
                Aggregation.group("jury.numero")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "nouveauBachelier", Document.class);

        List<String> numeros = results.getMappedResults().stream()
                .map(d -> d.getString("_id"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("Numéros de jury avec 2ème groupe: {}", numeros.size());

        if (numeros.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Récupérer les jurys complets (avec centre/ville/academie déjà dans Jury.centre)
        List<Jury> jurys = mongoTemplate.find(
                Query.query(Criteria.where("numero").in(numeros)), Jury.class
        );

        log.info("=== findJuryNumerosAvec2emeGroupe() - Fin, {} jury(s) ===", jurys.size());
        return jurys;
    }
}