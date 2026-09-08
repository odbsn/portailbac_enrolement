package com.officedubac.project.module.bacheliersToCampusen;

import com.github.pjfanning.xlsx.StreamingReader;
import com.officedubac.project.module.bacheliersToCampusen.error.ImportException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportBacheliersService {

    private static final Logger log = LoggerFactory.getLogger(ImportBacheliersService.class);
    private static final int BATCH_SIZE = 1000;
    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final BacheliersAdmisRepository candidatRepository;
    private final ImportLogRepository importLogRepository;
    private final MongoTemplate mongoTemplate;
    private final DataFormatter formatter = new DataFormatter(java.util.Locale.FRANCE);

    private static final Map<String, BiConsumer<BacheliersToCampusen, String>> COMMON_HEADERS = new HashMap<>();

    static {
        COMMON_HEADERS.put("n° table", BacheliersToCampusen::setNumeroTable);
        COMMON_HEADERS.put("prénom(s)", BacheliersToCampusen::setPrenoms);
        COMMON_HEADERS.put("nom", BacheliersToCampusen::setNom);
        COMMON_HEADERS.put("date nais.", BacheliersToCampusen::setDateNaissance);
        COMMON_HEADERS.put("année nais.", BacheliersToCampusen::setAnneeNaissance);
        COMMON_HEADERS.put("lieu de naissance", BacheliersToCampusen::setLieuNaissance);
        COMMON_HEADERS.put("telephone", BacheliersToCampusen::setTelephone);
        COMMON_HEADERS.put("sexe", BacheliersToCampusen::setSexe);
        COMMON_HEADERS.put("série", (c, v) -> { /* deja portee par le nom de la feuille */ });
        COMMON_HEADERS.put("ets. de provenance", BacheliersToCampusen::setEtsProvenance);
        COMMON_HEADERS.put("type cand.", BacheliersToCampusen::setTypeCandidature);
        COMMON_HEADERS.put("acad. de l'.ets prov", BacheliersToCampusen::setAcademieProvenance);
        COMMON_HEADERS.put("résidence", BacheliersToCampusen::setResidence);
        COMMON_HEADERS.put("centre d'ecrit", BacheliersToCampusen::setCentreEcrit);
        COMMON_HEADERS.put("n° jury", BacheliersToCampusen::setNumeroJury);
        COMMON_HEADERS.put("nbre. fois", BacheliersToCampusen::setNombreFois);
        COMMON_HEADERS.put("nationalité", BacheliersToCampusen::setNationalite);
        COMMON_HEADERS.put("mat. opt. 1", BacheliersToCampusen::setMatiereOptionnelle1);
        COMMON_HEADERS.put("mat. opt. 2", BacheliersToCampusen::setMatiereOptionnelle2);
        COMMON_HEADERS.put("mat. opt. 3", BacheliersToCampusen::setMatiereOptionnelle3);
        COMMON_HEADERS.put("epr. fa. liste a", BacheliersToCampusen::setEpreuveFacultativeListeA);
        COMMON_HEADERS.put("epr. fa. liste b", BacheliersToCampusen::setEpreuveFacultativeListeB);
        COMMON_HEADERS.put("note ef a", BacheliersToCampusen::setNoteEpreuveFacultativeA);
        COMMON_HEADERS.put("note ef b", BacheliersToCampusen::setNoteEpreuveFacultativeB);
        COMMON_HEADERS.put("note eps", BacheliersToCampusen::setNoteEps);
        COMMON_HEADERS.put("présent", BacheliersToCampusen::setPresent);
        COMMON_HEADERS.put("mention", BacheliersToCampusen::setMention);
        COMMON_HEADERS.put("résultat", BacheliersToCampusen::setResultat);
        COMMON_HEADERS.put("groupe résultat", BacheliersToCampusen::setGroupeResultat);
        COMMON_HEADERS.put("date de délibération", BacheliersToCampusen::setDateDeliberation);
        COMMON_HEADERS.put("pays de naissance", BacheliersToCampusen::setPaysNaissance);
        COMMON_HEADERS.put("cec", BacheliersToCampusen::setCec);
        COMMON_HEADERS.put("n° aec", BacheliersToCampusen::setNumeroAec);
        COMMON_HEADERS.put("année de l'extrait ec", BacheliersToCampusen::setAnneeExtraitEc);
        COMMON_HEADERS.put("type aec", BacheliersToCampusen::setTypeAec);
        COMMON_HEADERS.put("moy. 2nde.", BacheliersToCampusen::setMoyenneSeconde);
        COMMON_HEADERS.put("moy. 1ère", BacheliersToCampusen::setMoyennePremiere);
        COMMON_HEADERS.put("moy. s1 term.", BacheliersToCampusen::setMoyenneS1Terminale);
        COMMON_HEADERS.put("moy. s2 term.", BacheliersToCampusen::setMoyenneS2Terminale);
        COMMON_HEADERS.put("tot. pts au grp. 1", BacheliersToCampusen::setTotalPointsGroupe1);
        COMMON_HEADERS.put("moyenne au grp. 1", BacheliersToCampusen::setMoyenneGroupe1);
        COMMON_HEADERS.put("tot. pts. g1 et g2", BacheliersToCampusen::setTotalPointsG1G2);
        COMMON_HEADERS.put("moy. gle", BacheliersToCampusen::setMoyenneGenerale);
        COMMON_HEADERS.put("moy. sur mat.fond.", BacheliersToCampusen::setMoyenneMatieresFondamentales);
        COMMON_HEADERS.put("moy. retenue", BacheliersToCampusen::setMoyenneRetenue);
        COMMON_HEADERS.put("moy. déf.", BacheliersToCampusen::setMoyenneDefinitive);
    }

    public ImportLog importer(MultipartFile fichier, int annee, boolean remplacer)
    {
        if (remplacer) {
            long supprimes = candidatRepository.deleteByAnnee(annee);
            importLogRepository.deleteByAnnee(annee);
            log.info("Réimport complet de l'année {} : {} candidats supprimés", annee, supprimes);
        }

        // Ne relit un éventuel journal existant que si on ne vient pas de le supprimer :
        // évite en plus de faire planter l'import si un ancien document (schéma obsolète) subsiste.
        Optional<ImportLog> logExistant = remplacer ? Optional.empty() : findLogExistantSansEchouer(annee);

        Map<String, SerieImportStat> parSerie = new LinkedHashMap<>();
        long total = 0, nouveaux = 0, misAJour = 0;
        int feuilles = 0;

        try (InputStream is = fichier.getInputStream();
             Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(200)
                     .bufferSize(8192)
                     .open(is)) {

            for (Sheet sheet : workbook) {
                long[] r = importerFeuille(sheet, annee);
                parSerie.put(sheet.getSheetName(), new SerieImportStat(r[0], r[1], r[2]));
                total += r[0];
                nouveaux += r[1];
                misAJour += r[2];
                feuilles++;
                log.info("Feuille '{}' : {} candidats traités ({} nouveaux, {} mis à jour)",
                        sheet.getSheetName(), r[0], r[1], r[2]);
            }
        } catch (Exception e) {
            throw new ImportException("Échec de l'import du fichier Excel : " + e.getMessage(), e);
        }

        ImportLog importLog = logExistant.orElseGet(ImportLog::new);
        importLog.setAnnee(annee);
        importLog.setNomFichier(fichier.getOriginalFilename());
        importLog.setNombreFeuilles(feuilles);
        importLog.setNombreCandidats(total);
        importLog.setNombreNouveaux(nouveaux);
        importLog.setNombreMisAJour(misAJour);
        importLog.setCandidatsParSerie(parSerie);
        importLog.setDateImport(Instant.now());
        log.info("Import terminé pour l'année {} : {} traités ({} nouveaux, {} mis à jour)",
                annee, total, nouveaux, misAJour);
        return importLogRepository.save(importLog);
    }

    /** Comme annee est indexé unique, un document illisible (ancien schéma) doit être
     *  supprimé — pas seulement ignoré côté Java — sinon l'insertion du nouveau journal
     *  échouerait sur une contrainte d'unicité. */
    private Optional<ImportLog> findLogExistantSansEchouer(int annee) {
        try {
            return importLogRepository.findByAnnee(annee);
        } catch (Exception e) {
            log.warn("Journal d'import illisible pour l'année {} (schéma obsolète) — recréation. Cause : {}",
                    annee, e.getMessage());
            importLogRepository.deleteByAnnee(annee);
            return Optional.empty();
        }
    }

    private long[] importerFeuille(Sheet sheet, int annee) {
        String serie = sheet.getSheetName().trim();
        List<String> headers = null;
        List<BacheliersToCampusen> batch = new ArrayList<>(BATCH_SIZE);
        long total = 0, nouveaux = 0, misAJour = 0;

        for (Row row : sheet) {
            if (headers == null) {
                if (estVide(row)) continue;
                headers = lireEntetes(row);
                continue;
            }
            BacheliersToCampusen c = lireCandidat(row, headers, annee, serie);
            if (c == null) continue;
            batch.add(c);
            if (batch.size() >= BATCH_SIZE) {
                long[] r = upsertBatch(batch);
                total += r[0]; nouveaux += r[1]; misAJour += r[2];
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            long[] r = upsertBatch(batch);
            total += r[0]; nouveaux += r[1]; misAJour += r[2];
        }
        return new long[]{total, nouveaux, misAJour};
    }

    /** Vérifie l'existence par numeroTable seul (données sessionnières : un numéro de table
     *  n'appartient qu'à une session) puis met à jour ou insère en un seul bulk write.
     *  Les lignes sans numeroTable (rares) sont simplement insérées, sans vérification. */
    private long[] upsertBatch(List<BacheliersToCampusen> batch) {
        List<BacheliersToCampusen> avecNumero = new ArrayList<>();
        List<BacheliersToCampusen> sansNumero = new ArrayList<>();
        for (BacheliersToCampusen c : batch) {
            (c.getNumeroTable() != null ? avecNumero : sansNumero).add(c);
        }

        Set<String> numerosTable = avecNumero.stream()
                .map(BacheliersToCampusen::getNumeroTable)
                .collect(Collectors.toSet());

        Set<String> existants = numerosTable.isEmpty() ? Collections.emptySet() : new HashSet<>(mongoTemplate.findDistinct(
                Query.query(Criteria.where("numeroTable").in(numerosTable)),
                "numeroTable", BacheliersToCampusen.class, String.class));

        long nouveaux = 0, misAJour = 0;

        if (!avecNumero.isEmpty()) {
            BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, BacheliersToCampusen.class);
            for (BacheliersToCampusen c : avecNumero) {
                Query query = Query.query(Criteria.where("numeroTable").is(c.getNumeroTable()));
                bulkOps.upsert(query, construireUpdate(c));
                if (existants.contains(c.getNumeroTable())) {
                    misAJour++;
                } else {
                    nouveaux++;
                }
            }
            bulkOps.execute();
        }
        if (!sansNumero.isEmpty()) {
            mongoTemplate.insert(sansNumero, BacheliersToCampusen.class);
            nouveaux += sansNumero.size();
        }
        return new long[]{batch.size(), nouveaux, misAJour};
    }

    private Update construireUpdate(BacheliersToCampusen c) {
        return new Update()
                .set("serie", c.getSerie())
                .set("prenoms", c.getPrenoms())
                .set("nom", c.getNom())
                .set("dateNaissance", c.getDateNaissance())
                .set("anneeNaissance", c.getAnneeNaissance())
                .set("lieuNaissance", c.getLieuNaissance())
                .set("telephone", c.getTelephone())
                .set("sexe", c.getSexe())
                .set("etsProvenance", c.getEtsProvenance())
                .set("typeCandidature", c.getTypeCandidature())
                .set("academieProvenance", c.getAcademieProvenance())
                .set("residence", c.getResidence())
                .set("centreEcrit", c.getCentreEcrit())
                .set("numeroJury", c.getNumeroJury())
                .set("nombreFois", c.getNombreFois())
                .set("nationalite", c.getNationalite())
                .set("matiereOptionnelle1", c.getMatiereOptionnelle1())
                .set("matiereOptionnelle2", c.getMatiereOptionnelle2())
                .set("matiereOptionnelle3", c.getMatiereOptionnelle3())
                .set("epreuveFacultativeListeA", c.getEpreuveFacultativeListeA())
                .set("epreuveFacultativeListeB", c.getEpreuveFacultativeListeB())
                .set("noteEpreuveFacultativeA", c.getNoteEpreuveFacultativeA())
                .set("noteEpreuveFacultativeB", c.getNoteEpreuveFacultativeB())
                .set("noteEps", c.getNoteEps())
                .set("present", c.getPresent())
                .set("mention", c.getMention())
                .set("resultat", c.getResultat())
                .set("groupeResultat", c.getGroupeResultat())
                .set("dateDeliberation", c.getDateDeliberation())
                .set("paysNaissance", c.getPaysNaissance())
                .set("cec", c.getCec())
                .set("numeroAec", c.getNumeroAec())
                .set("anneeExtraitEc", c.getAnneeExtraitEc())
                .set("typeAec", c.getTypeAec())
                .set("moyenneSeconde", c.getMoyenneSeconde())
                .set("moyennePremiere", c.getMoyennePremiere())
                .set("moyenneS1Terminale", c.getMoyenneS1Terminale())
                .set("moyenneS2Terminale", c.getMoyenneS2Terminale())
                .set("totalPointsGroupe1", c.getTotalPointsGroupe1())
                .set("moyenneGroupe1", c.getMoyenneGroupe1())
                .set("totalPointsG1G2", c.getTotalPointsG1G2())
                .set("moyenneGenerale", c.getMoyenneGenerale())
                .set("moyenneMatieresFondamentales", c.getMoyenneMatieresFondamentales())
                .set("moyenneRetenue", c.getMoyenneRetenue())
                .set("moyenneDefinitive", c.getMoyenneDefinitive())
                .set("notes", c.getNotes())
                .setOnInsert("annee", c.getAnnee())
                .setOnInsert("numeroTable", c.getNumeroTable());
    }

    private List<String> lireEntetes(Row row) {
        List<String> headers = new ArrayList<>();
        short last = row.getLastCellNum();
        Set<String> vus = new HashSet<>();
        for (int i = 0; i < last; i++) {
            Cell cell = row.getCell(i);
            String h = cell == null ? "" : nettoyer(formatter.formatCellValue(cell));
            String unique = h;
            int suffixe = 2;
            while (!unique.isEmpty() && !vus.add(unique.toLowerCase())) {
                unique = h + " (" + suffixe++ + ")";
            }
            headers.add(unique);
        }
        return headers;
    }

    private BacheliersToCampusen lireCandidat(Row row, List<String> headers, int annee, String serie) {
        if (estVide(row)) return null;

        BacheliersToCampusen c = new BacheliersToCampusen();
        c.setAnnee(annee);
        c.setSerie(serie);

        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header.isEmpty()) continue;
            String valeur = valeurCellule(row.getCell(i));
            if (valeur == null || valeur.isEmpty()) continue;

            BiConsumer<BacheliersToCampusen, String> setter = COMMON_HEADERS.get(header.toLowerCase());
            if (setter != null) {
                setter.accept(c, valeur);
            } else {
                c.getNotes().put(header, valeur);
            }
        }
        if (c.getNumeroTable() == null && c.getNom() == null) return null;
        return c;
    }

    private String valeurCellule(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_FR);
        }
        String v = formatter.formatCellValue(cell);
        return v == null ? null : v.trim();
    }

    private String nettoyer(String header) {
        if (header == null) return "";
        return header.replace("-\n", "")
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean estVide(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String v = formatter.formatCellValue(cell);
                if (v != null && !v.isBlank()) return false;
            }
        }
        return true;
    }
}