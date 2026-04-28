package com.officedubac.project.services;

import com.officedubac.project.dto.*;
import com.officedubac.project.exception.GlobalHandlerControllerException;
import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.repository.CandidatRepository;
import com.officedubac.project.repository.EtablissementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsService
{
    private final MongoTemplate mongoTemplate;

    @Autowired
    private final CandidatRepository candidatRepository;

    @Autowired
    private final EtablissementRepository etablissementRepository;

    public List<OperatorDailyCountDTO> countDailyByOperator(LocalDate startDate, LocalDate endDate, Integer session)
    {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        MatchOperation match = Aggregation.match(
                Criteria.where("dateOperation").gte(startDateTime).lte(endDateTime)
                        .and("session").is(session)
        );

        GroupOperation group = Aggregation.group("operator", "dateOperation")
                .sum(ConditionalOperators.when(Criteria.where("decision").is(1)).then(1).otherwise(0))
                .as("accepted")
                .sum(ConditionalOperators.when(Criteria.where("decision").is(2)).then(1).otherwise(0))
                .as("rejected");

        ProjectionOperation project = Aggregation.project()
                .and("_id.operator").as("operator")
                .and("_id.dateOperation").as("dateOperation")
                .andInclude("accepted", "rejected");

        SortOperation sort = Aggregation.sort(Sort.by(Sort.Direction.ASC, "dateOperation"));

        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        AggregationResults<OperatorDailyCountDTO> results =
                mongoTemplate.aggregate(aggregation, "candidat", OperatorDailyCountDTO.class);

        return results.getMappedResults();
    }

    public List<MapDTO> getStatsParDepartement(int session)
    {
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        // Étape 1 : Groupement par département
        GroupOperation group = Aggregation.group("etablissement.departement.name")
                .count().as("totalCandidats")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("totalM")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("totalF")
                .sum(ConditionalOperators.when(Criteria.where("decision").is(0)).then(1).otherwise(0)).as("enAttente")
                .sum(ConditionalOperators.when(Criteria.where("decision").is(1)).then(1).otherwise(0)).as("valider")
                .sum(ConditionalOperators.when(Criteria.where("decision").is(2)).then(1).otherwise(0)).as("rejeter");

        // Étape 2 : Projection
        ProjectionOperation project = Aggregation.project()
                .and("_id").as("departement")
                .andInclude("totalCandidats", "totalM", "totalF", "enAttente", "valider", "rejeter");

        // Étape 3 : Tri
        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "departement");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        // Exécution de l'agrégation
        AggregationResults<MapDTO> results = mongoTemplate.aggregate(aggregation, "candidat", MapDTO.class);

        // Retourner les résultats mappés
        return results.getMappedResults();
    }


    public GlobalStatVCDTO getGlobalStatVC(int session) {
        // Étape 0 : Filtrage par session
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        // Étape 1 : Groupement global (somme des champs numériques)
        GroupOperation group = Aggregation.group()
                .sum("count_1000_OB").as("vOB")
                .sum("count_5000").as("v5000")
                .sum("count_1000_EF").as("v1000EF");

        // Étape 2 : Projection (renommer ou inclure les champs désirés)
        ProjectionOperation project = Aggregation.project().andInclude("vOB", "v5000", "v1000EF");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project);

        // Exécution de l'agrégation
        GlobalStatVCDTO result = mongoTemplate.aggregate(
                aggregation, "compte_droits_inscription", GlobalStatVCDTO.class).getUniqueMappedResult();

        // Retour du résultat unique (ou null si vide)
        return result != null ? result : new GlobalStatVCDTO(0, 0, 0);
    }

    public GlobalStatDTO getGlobalStat(int session)
    {
        // Étape 0 : Filtrage par session
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        // Étape 1 : Groupement global (pas par département)
        GroupOperation group = Aggregation.group()
                .count().as("candidats")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        // Étape 2 : Projection pour renommer proprement
        ProjectionOperation project = Aggregation.project()
                .andInclude("candidats", "male", "female");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project);

        // Exécution de l'agrégation
        GlobalStatDTO result = mongoTemplate.aggregate(aggregation, "candidat", GlobalStatDTO.class)
                .getUniqueMappedResult();

        // Retour du résultat unique (ou null si vide)
        return result != null ? result : new GlobalStatDTO(0, 0, 0);
    }

    public GlobalStatDTO getGlobalStatIA(int session, String ia)
    {
        // Étape 0 : Filtrage par session
        MatchOperation match = Aggregation.match(Criteria.where("session")
                .is(session)
                .and("etablissement.inspectionAcademie.code").is(ia));

        // Étape 1 : Groupement global (pas par département)
        GroupOperation group = Aggregation.group()
                .count().as("candidats")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        // Étape 2 : Projection pour renommer proprement
        ProjectionOperation project = Aggregation.project()
                .andInclude("candidats", "male", "female");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project);

        // Exécution de l'agrégation
        GlobalStatDTO result = mongoTemplate.aggregate(aggregation, "candidat", GlobalStatDTO.class)
                .getUniqueMappedResult();

        // Retour du résultat unique (ou null si vide)
        return result != null ? result : new GlobalStatDTO(0, 0, 0);
    }

    public List<EtablissementSummaryReception> summarizeByIA(Long session, String ia) {

        List<Etablissement> etablissements = new ArrayList<>();
        Map<String, Etablissement> etabById = etablissementRepository
                .findByInspectionAcademieCode(ia)
                .stream()
                .peek(etablissements::add)  // remplit la liste en même temps
                .collect(Collectors.toMap(Etablissement::getId, Function.identity()));

        List<Candidat> candidats =
                candidatRepository.findBySessionAndEtablissementInspectionAcademieCode(session, ia);

        // 4. Regrouper fonctionnellement les candidats appartenant à l'IA
        Map<Etablissement, EtablissementSummaryReception> summaries =
                candidats.stream()
                        .filter(c -> c.getEtablissement() != null)
                        .filter(c -> etabById.containsKey(c.getEtablissement().getId()))
                        .collect(Collectors.toMap(
                                Candidat::getEtablissement,
                                c -> {
                                    EtablissementSummaryReception s = new EtablissementSummaryReception();
                                    s.setEtablissement(c.getEtablissement());
                                    updateSummary(s, c.getDecision());
                                    return s;
                                },
                                (s1, s2) -> { // Merge en cas de même établissement
                                    s2.copyInto(s1);
                                    return s1;
                                }
                        ));

        // 5. Ajouter les établissements sans candidats (approche fonctionnelle)
        etablissements.forEach(etab ->
                summaries.putIfAbsent(etab, emptySummary(etab))
        );

        return new ArrayList<>(summaries.values());
    }

    /** Ajout propre des compteurs */
    private void updateSummary(EtablissementSummaryReception s, int decision) {
        switch (decision) {
            case 1 -> s.incrementDecision1();
            case 2 -> s.incrementDecision2();
            default -> s.incrementDecision0();
        }
    }

    /** Création fonctionnelle d’un summary vide */
    private EtablissementSummaryReception emptySummary(Etablissement etab) {
        EtablissementSummaryReception s = new EtablissementSummaryReception();
        s.setEtablissement(etab);
        return s;
    }



    public List<StatAcademieDTO> getStatsParAcademie(int session)
    {
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        // Étape 1 : Groupement par département
        GroupOperation group = Aggregation.group("etablissement.inspectionAcademie.code")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        // Étape 2 : Projection
        ProjectionOperation project = Aggregation.project()
                .and("_id").as("academia")
                .andInclude( "male", "female");

        // Étape 3 : Tri
        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "academia");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        // Exécution de l'agrégation
        AggregationResults<StatAcademieDTO> results = mongoTemplate.aggregate(aggregation, "candidat", StatAcademieDTO.class);

        // Retourner les résultats mappés
        return results.getMappedResults();
    }


    public List<StatAcademieDTO> getStatsCGSParAcademie_(int session)
    {
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        GroupOperation group = Aggregation.group("etablissement.inspectionAcademie.code")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("academia")   // récupération du group
                .andInclude("male", "female");

        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "academia");

        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        AggregationResults<StatAcademieDTO> results =
                mongoTemplate.aggregate(aggregation, "concours_general", StatAcademieDTO.class);

        return results.getMappedResults();
    }

    public List<StatAcademieDTO> getStatsCGSTypeEtab(int session)
    {
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        GroupOperation group = Aggregation.group("etablissement.typeEtablissement.code")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("academia")   // récupération du group
                .andInclude("male", "female");

        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "academia");

        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        AggregationResults<StatAcademieDTO> results =
                mongoTemplate.aggregate(aggregation, "concours_general", StatAcademieDTO.class);

        return results.getMappedResults();
    }


    public List<StatAcademieDTO> getStatsTypeEtab(int session)
    {
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        GroupOperation group = Aggregation.group("etablissement.typeEtablissement.code")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("academia")   // récupération du group
                .andInclude("male", "female");

        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "academia");

        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        AggregationResults<StatAcademieDTO> results =
                mongoTemplate.aggregate(aggregation, "candidat", StatAcademieDTO.class);

        return results.getMappedResults();
    }

    public List<StatDisciplineDTO> getStatsSpeciaNiveau(int session, String level)
    {

        MatchOperation match = Aggregation.match(Criteria.where("session").is(session).and("level").is(level));

        // Étape 1 : Groupement par département
        GroupOperation group = Aggregation.group("specialite")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        // Étape 2 : Projection
        ProjectionOperation project = Aggregation.project()
                .and("_id").as("discipline")
                .andInclude("male", "female");

        // Étape 3 : Tri
        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "discipline");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        // Exécution de l'agrégation
        AggregationResults<StatDisciplineDTO> results = mongoTemplate.aggregate(aggregation, "concours_general", StatDisciplineDTO.class);

        // Retourner les résultats mappés
        return results.getMappedResults();
    }



    public List<StatHandicapDTO> getStatsParHandicap(int session)
    {
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        // Étape 1 : Groupement par département
        GroupOperation group = Aggregation.group("type_handicap")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        // Étape 2 : Projection
        ProjectionOperation project = Aggregation.project()
                .and("_id").as("handicap")
                .andInclude("male", "female");

        // Étape 3 : Tri
        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "handicap");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        // Exécution de l'agrégation
        AggregationResults<StatHandicapDTO> results = mongoTemplate.aggregate(aggregation, "candidat", StatHandicapDTO.class);

        // Retourner les résultats mappés
        return results.getMappedResults();
    }


    public List<StatSerieDTO> getStatsParSerie(int session)
    {
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        // Étape 1 : Groupement par département
        GroupOperation group = Aggregation.group("serie.code")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        // Étape 2 : Projection
        ProjectionOperation project = Aggregation.project()
                .and("_id").as("serie")
                .andInclude("male", "female");

        // Étape 3 : Tri
        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "serie");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        // Exécution de l'agrégation
        AggregationResults<StatSerieDTO> results = mongoTemplate.aggregate(aggregation, "candidat", StatSerieDTO.class);

        // Retourner les résultats mappés
        return results.getMappedResults();
    }


    public List<StatSerieDTO> getStatsForLitteraire(int session)
    {
        MatchOperation match = Aggregation.match(
                Criteria.where("session").is(session)
                        .and("serie.typeFiliere.name").is("Littéraire")
        );
        // Étape 1 : Groupement par département
        GroupOperation group = Aggregation.group("serie.code")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        // Étape 2 : Projection
        ProjectionOperation project = Aggregation.project()
                .and("_id").as("serie")
                .andInclude("male", "female");

        // Étape 3 : Tri
        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "serie");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        // Exécution de l'agrégation
        AggregationResults<StatSerieDTO> results = mongoTemplate.aggregate(aggregation, "candidat", StatSerieDTO.class);

        // Retourner les résultats mappés
        return results.getMappedResults();
    }


    public List<StatSerieDTO> getStatsForScience(int session)
    {
        MatchOperation match = Aggregation.match(
                Criteria.where("session").is(session)
                        .and("serie.typeFiliere.name").ne("Littéraire")
        );
        // Étape 1 : Groupement par département
        GroupOperation group = Aggregation.group("serie.code")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("male")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("female");

        // Étape 2 : Projection
        ProjectionOperation project = Aggregation.project()
                .and("_id").as("serie")
                .andInclude("male", "female");

        // Étape 3 : Tri
        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "serie");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort);

        // Exécution de l'agrégation
        AggregationResults<StatSerieDTO> results = mongoTemplate.aggregate(aggregation, "candidat", StatSerieDTO.class);

        // Retourner les résultats mappés
        return results.getMappedResults();
    }


    public GlobalStatDTO getGlobalStat_(int session)
    {
        // Étape 0 : Filtrage par session
        MatchOperation match = Aggregation.match(Criteria.where("session").is(session));

        // Étape 1 : Groupement global (pas par département)
        GroupOperation group = Aggregation.group()
                .count().as("candidats")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("M")).then(1).otherwise(0)).as("M")
                .sum(ConditionalOperators.when(Criteria.where("gender").is("F")).then(1).otherwise(0)).as("F");

        // Étape 2 : Projection pour renommer proprement
        ProjectionOperation project = Aggregation.project()
                .and("candidats").as("candidats")
                .and("M").as("male")
                .and("F").as("female");

        // Création du pipeline d'agrégation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project);

        // Exécution de l'agrégation
        GlobalStatDTO result = mongoTemplate.aggregate(aggregation, "concours_general", GlobalStatDTO.class)
                .getUniqueMappedResult();

        // Retour du résultat unique (ou null si vide)
        return result != null ? result : new GlobalStatDTO(0, 0, 0);
    }










}
