package com.officedubac.project.services;

import com.officedubac.project.config.ResultatLabels;
import com.officedubac.project.config.SerieReferentiel;
import com.officedubac.project.dto.*;
import com.officedubac.project.module.candidatFinis.CandidatFinis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.ToLongFunction;

@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private static final String SEXE_FILLE = "F";
    private static final String SEXE_GARCON = "M";

    private final MongoTemplate mongoTemplate;


    public RecapTableDTO getRecapTable(String academie, Integer session) {
        Map<String, Map<String, SerieSexeCounts>> grouped = computeGroupedCounts(academie, session);
        List<String> series = orderedSeries(grouped.keySet());
        List<StatBlock> blocs = new ArrayList<>();

        blocs.add(build3RowBlock("Inscrits", "Total inscrits", series, grouped, c -> c.inscrits));
        blocs.add(build3RowBlock("Présents", "Total présents", series, grouped, c -> c.presents));
        blocs.add(build3RowBlock("Admis d'Office", "Total Admis", series, grouped, c -> c.admisOffice));
        blocs.add(build3RowBlock("Mention Tbien", "Total TB", series, grouped, c -> c.mentionTB));
        blocs.add(build3RowBlock("Mention Bien", "Total Bien", series, grouped, c -> c.mentionB));
        blocs.add(build3RowBlock("Mention Abien", "Total Abien", series, grouped, c -> c.mentionAB));
        blocs.add(build1RowBlock("Total Mentions", "Total Mentions", series, grouped,
                c -> c.mentionTB + c.mentionB + c.mentionAB));
        blocs.add(build1RowBlock("Autorisés à faire le 2nd groupe", "Autorisés à faire le 2nd groupe",
                series, grouped, c -> c.autorise2nd));
        blocs.add(build3RowBlock("Admis 2nd Groupe", "Admis 2nd Grpe", series, grouped, c -> c.admis2nd));
        blocs.add(build3RowBlock("Total Admis", "Total Admis", series, grouped,
                c -> c.admisOffice + c.admis2nd));

        return new RecapTableDTO(academie == null || academie.isBlank() ? "TOUTES" : academie, series, blocs);
    }

    public KpiSummaryDTO getKpiSummary(String academie, Integer session) {
        Map<String, Map<String, SerieSexeCounts>> grouped = computeGroupedCounts(academie, session);

        long totalInscrits = 0, totalPresents = 0, totalAdmisOffice = 0, totalAdmis2nd = 0, totalMentions = 0;
        Map<String, Long> repartitionFiliere = new LinkedHashMap<>();
        repartitionFiliere.put(SerieReferentiel.FILIERE_ST, 0L);
        repartitionFiliere.put(SerieReferentiel.FILIERE_TERTIAIRE, 0L);
        repartitionFiliere.put(SerieReferentiel.FILIERE_LITTERAIRE, 0L);

        for (Map.Entry<String, Map<String, SerieSexeCounts>> serieEntry : grouped.entrySet()) {
            String serie = serieEntry.getKey();
            String filiere = SerieReferentiel.FILIERE_PAR_SERIE.getOrDefault(filiereKey(serie), "Autre");
            long inscritsSerie = 0;
            for (SerieSexeCounts c : serieEntry.getValue().values()) {
                totalInscrits += c.inscrits;
                totalPresents += c.presents;
                totalAdmisOffice += c.admisOffice;
                totalAdmis2nd += c.admis2nd;
                totalMentions += c.mentionTB + c.mentionB + c.mentionAB;
                inscritsSerie += c.inscrits;
            }
            repartitionFiliere.merge(filiere, inscritsSerie, Long::sum);
        }

        long totalAdmis = totalAdmisOffice + totalAdmis2nd;
        long totalAbsents = totalInscrits - totalPresents;
        double tauxReussite = totalPresents == 0 ? 0.0
                : Math.round((totalAdmis * 10000.0) / totalPresents) / 100.0;

        return new KpiSummaryDTO(
                academie == null || academie.isBlank() ? "TOUTES" : academie,
                totalInscrits, totalPresents, totalAbsents, totalAdmis, totalMentions,
                tauxReussite, repartitionFiliere
        );
    }

    public List<SerieChartDataDTO> getSerieChartData(String academie, Integer session) {
        Map<String, Map<String, SerieSexeCounts>> grouped = computeGroupedCounts(academie, session);
        List<SerieChartDataDTO> result = new ArrayList<>();
        for (String serie : orderedSeries(grouped.keySet())) {
            long inscrits = 0, presents = 0, admis = 0;
            for (SerieSexeCounts c : grouped.get(serie).values()) {
                inscrits += c.inscrits;
                presents += c.presents;
                admis += c.admisOffice + c.admis2nd;
            }
            result.add(new SerieChartDataDTO(serie, inscrits, presents, admis));
        }
        return result;
    }

    public List<String> getAcademiesDisponibles() {
        List<String> distinct = mongoTemplate.findDistinct("academieProvenance", CandidatFinis.class, String.class);

        List<String> academies = new ArrayList<>(new TreeSet<>(
                distinct.stream().filter(a -> a != null && !a.isBlank()).toList()
        ));
        academies.add(0, "TOUTES");
        return academies;
    }

    // ------------------------------------------------------------------
    // Agrégation Mongo (jointure + aplatissement) puis comptage en Java
    // ------------------------------------------------------------------

    private Map<String, Map<String, SerieSexeCounts>> computeGroupedCounts(String academie, Integer session) {
        List<CandidatStatProjection> projections = fetchProjections(academie, session);

        Map<String, Map<String, SerieSexeCounts>> grouped = new LinkedHashMap<>();
        for (CandidatStatProjection p : projections) {
            String serie = p.getSerie() == null ? "INDETERMINE" : p.getSerie();
            String sexe = p.getSexe() == null ? SEXE_GARCON : p.getSexe();

            SerieSexeCounts counts = grouped
                    .computeIfAbsent(serie, k -> new LinkedHashMap<>())
                    .computeIfAbsent(sexe, k -> new SerieSexeCounts());

            counts.inscrits++;
            boolean absent = "1".equals(p.getAbsence());
            if (absent) {
                continue;
            }
            counts.presents++;

            String resultat = p.getResultat();
            if (ResultatLabels.ADMIS.equals(resultat)) {
                counts.admisOffice++;
                String mention = p.getMention();
                if (ResultatLabels.MENTION_TRES_BIEN.equals(mention)) {
                    counts.mentionTB++;
                } else if (ResultatLabels.MENTION_BIEN.equals(mention)) {
                    counts.mentionB++;
                } else if (ResultatLabels.MENTION_ASSEZ_BIEN.equals(mention)) {
                    counts.mentionAB++;
                }
            } else if (ResultatLabels.AUTORISE_2ND_GROUPE.equals(resultat)) {
                counts.autorise2nd++;
            } else if (ResultatLabels.ADMIS_2ND_GROUPE.equals(resultat)) {
                counts.admis2nd++;
            }
            // Tout autre cas (resultat == "--------" ou null) : présent, mais
            // pas encore délibéré -> ne rentre dans aucun compteur de résultat.
        }
        return grouped;
    }

    private List<CandidatStatProjection> fetchProjections(String academie, Integer session) {
        List<AggregationOperation> ops = new ArrayList<>();

        List<Criteria> filters = new ArrayList<>();
        if (academie != null && !academie.isBlank() && !"TOUTES".equalsIgnoreCase(academie)) {
            filters.add(Criteria.where("academieEcrit").is(academie));
        }
        if (session != null) {
            filters.add(Criteria.where("session").is(session));
        }
        if (!filters.isEmpty()) {
            Criteria combined = filters.size() == 1
                    ? filters.get(0)
                    : new Criteria().andOperator(filters.toArray(new Criteria[0]));
            ops.add(Aggregation.match(combined));
        }

        // Jointure avec la collection résultats sur numeroTable (même type
        // String des deux côtés -> indispensable pour que le $lookup matche).
        ops.add(Aggregation.lookup("nouveauBachelier", "numeroTable", "numeroTable", "candidat_finis"));
        // preserveNullAndEmptyArrays = true : un candidat sans résultat publié
        // ne doit pas être exclu, juste avoir resultat/mention = null.
        ops.add(Aggregation.unwind("candidat_finis", true));
        ops.add(Aggregation.project()
                .and("serie").as("serie")
                .and("sexe").as("sexe")
                .and("absence").as("absence")
                .and("resultatInfo.jury.resultat").as("resultat")
                .and("resultatInfo.jury.mention").as("mention"));

        Aggregation aggregation = Aggregation.newAggregation(ops);
        AggregationResults<CandidatStatProjection> results =
                mongoTemplate.aggregate(aggregation, CandidatFinis.class, CandidatStatProjection.class);
        return results.getMappedResults();
    }

    private StatBlock build3RowBlock(String titre, String totalLabel, List<String> series,
                                      Map<String, Map<String, SerieSexeCounts>> grouped,
                                      ToLongFunction<SerieSexeCounts> extractor) {
        StatRow filles = buildRow("Filles", series, grouped, SEXE_FILLE, extractor);
        StatRow garcons = buildRow("Garçons", series, grouped, SEXE_GARCON, extractor);
        StatRow total = sumRows(totalLabel, series, filles, garcons);
        return new StatBlock(titre, List.of(filles, garcons, total));
    }

    private StatBlock build1RowBlock(String titre, String label, List<String> series,
                                      Map<String, Map<String, SerieSexeCounts>> grouped,
                                      ToLongFunction<SerieSexeCounts> extractor) {
        StatRow filles = buildRow("tmpF", series, grouped, SEXE_FILLE, extractor);
        StatRow garcons = buildRow("tmpG", series, grouped, SEXE_GARCON, extractor);
        StatRow total = sumRows(label, series, filles, garcons);
        return new StatBlock(titre, List.of(total));
    }

    private StatRow buildRow(String label, List<String> series,
                              Map<String, Map<String, SerieSexeCounts>> grouped,
                              String sexe, ToLongFunction<SerieSexeCounts> extractor) {
        Map<String, Long> valeurs = new LinkedHashMap<>();
        long total = 0;
        for (String serie : series) {
            SerieSexeCounts counts = grouped.getOrDefault(serie, Map.of()).get(sexe);
            long valeur = counts == null ? 0L : extractor.applyAsLong(counts);
            valeurs.put(serie, valeur);
            total += valeur;
        }
        return new StatRow(label, valeurs, total);
    }

    private StatRow sumRows(String label, List<String> series, StatRow a, StatRow b) {
        Map<String, Long> valeurs = new LinkedHashMap<>();
        for (String serie : series) {
            valeurs.put(serie, a.valeursParSerie().getOrDefault(serie, 0L) + b.valeursParSerie().getOrDefault(serie, 0L));
        }
        return new StatRow(label, valeurs, a.total() + b.total());
    }

    /** Ordonne les séries selon le référentiel officiel ; les inconnues sont ajoutées à la fin (tri alphabétique). */
    private List<String> orderedSeries(java.util.Set<String> seriesPresentes) {
        List<String> result = new ArrayList<>();
        for (String s : SerieReferentiel.ORDRE_SERIES) {
            if (seriesPresentes.contains(s)) {
                result.add(s);
            }
        }
        List<String> inconnues = new ArrayList<>(seriesPresentes);
        inconnues.removeAll(result);
        inconnues.remove("INDETERMINE");
        TreeSet<String> sortedInconnues = new TreeSet<>(inconnues);
        result.addAll(sortedInconnues);
        if (seriesPresentes.contains("INDETERMINE")) {
            result.add("INDETERMINE");
        }
        return result;
    }

    /** "L'1" contient une apostrophe qui ne doit pas casser la recherche dans la map filière (clé identique ici, mais on isole le point d'entrée pour faciliter un futur nettoyage de libellé). */
    private String filiereKey(String serie) {
        return serie;
    }

    /** Accumulateur mutable interne : un par couple (série, sexe). */
    private static final class SerieSexeCounts {
        long inscrits;
        long presents;
        long admisOffice;
        long mentionTB;
        long mentionB;
        long mentionAB;
        long autorise2nd;
        long admis2nd;
    }
}
