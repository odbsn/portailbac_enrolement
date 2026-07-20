package com.officedubac.project.module.bacheliersToCampusen;

import com.github.pjfanning.xlsx.StreamingReader;
import com.officedubac.project.module.bacheliersToCampusen.error.ConflictException;
import com.officedubac.project.module.bacheliersToCampusen.error.ImportException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class ImportBacheliersService {

    private static final Logger log = LoggerFactory.getLogger(ImportBacheliersService.class);
    private static final int BATCH_SIZE = 1000;
    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final BacheliersAdmisRepository candidatRepository;
    private final ImportLogRepository importLogRepository;
    private final DataFormatter formatter = new DataFormatter(java.util.Locale.FRANCE);

    private static final Map<String, BiConsumer<BacheliersToCampusen, String>> COMMON_HEADERS = new HashMap<>();

    static {
        COMMON_HEADERS.put("n° table", BacheliersToCampusen::setNumeroTable);
        COMMON_HEADERS.put("prénom(s)", BacheliersToCampusen::setPrenoms);
        COMMON_HEADERS.put("nom", BacheliersToCampusen::setNom);
        COMMON_HEADERS.put("date nais.", BacheliersToCampusen::setDateNaissance);
        COMMON_HEADERS.put("année nais.", BacheliersToCampusen::setAnneeNaissance);
        COMMON_HEADERS.put("lieu de naissance", BacheliersToCampusen::setLieuNaissance);
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

    public ImportLog importer(MultipartFile fichier, int annee, boolean remplacer) {
        if (importLogRepository.findByAnnee(annee).isPresent() || candidatRepository.existsByAnnee(annee)) {
            if (!remplacer) {
                throw new ConflictException("L'année " + annee
                        + " a déjà été importée (import one-shot). Relancez avec remplacer=true pour réimporter.");
            }
            long supprimes = candidatRepository.deleteByAnnee(annee);
            importLogRepository.deleteByAnnee(annee);
            log.info("Réimport de l'année {} : {} candidats supprimés", annee, supprimes);
        }

        Map<String, Long> parSerie = new LinkedHashMap<>();
        long total = 0;
        int feuilles = 0;

        try (InputStream is = fichier.getInputStream();
             Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(200)
                     .bufferSize(8192)
                     .open(is)) {

            for (Sheet sheet : workbook) {
                long n = importerFeuille(sheet, annee);
                parSerie.put(sheet.getSheetName(), n);
                total += n;
                feuilles++;
                log.info("Feuille '{}' : {} candidats importés", sheet.getSheetName(), n);
            }
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            // en cas d'echec au milieu de l'import, on nettoie pour rester coherent
            candidatRepository.deleteByAnnee(annee);
            throw new ImportException("Échec de l'import du fichier Excel : " + e.getMessage(), e);
        }

        ImportLog importLog = new ImportLog();
        importLog.setAnnee(annee);
        importLog.setNomFichier(fichier.getOriginalFilename());
        importLog.setNombreFeuilles(feuilles);
        importLog.setNombreCandidats(total);
        importLog.setCandidatsParSerie(parSerie);
        importLog.setDateImport(Instant.now());
        return importLogRepository.save(importLog);
    }

    private long importerFeuille(Sheet sheet, int annee) {
        String serie = sheet.getSheetName().trim();
        List<String> headers = null;
        List<BacheliersToCampusen> batch = new ArrayList<>(BATCH_SIZE);
        long count = 0;

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
                candidatRepository.saveAll(batch);
                count += batch.size();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            candidatRepository.saveAll(batch);
            count += batch.size();
        }
        return count;
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