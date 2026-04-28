package com.officedubac.project.services;

import com.officedubac.project.dto.*;
import com.officedubac.project.exception.TechnicalException;
import com.officedubac.project.models.*;
import com.officedubac.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcoursGeneralService
{
    @Autowired
    private final SerieRepository serieRepository;

    @Autowired
    private final RegleCentreRepository regleCentreRepository;

    @Autowired
    private final ConcoursGeneralRepository concoursGeneralRepository;

    @Autowired
    private final ListeFinaleCndidatsCGSRepository listeFinaleCndidatsCGSRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<String> getCentresByAcademia(String academia, Long session) {
        List<ListeFinaleCandidatsCGS> candidats = listeFinaleCndidatsCGSRepository.findCentresByAcademiaAndSession(academia, session);

        return candidats.stream()
                .map(ListeFinaleCandidatsCGS::getCentreComposition)
                .distinct()
                .toList();
    }

    public List<String> getNiveauxByCentre(String centre, Long session) {
        List<ListeFinaleCandidatsCGS> candidats = listeFinaleCndidatsCGSRepository.findNiveauxByCentreAndSession(centre, session);
        //log.info(candidats.toString());

        return candidats.stream()
                .map(ListeFinaleCandidatsCGS::getLevel)
                .distinct()
                .toList();
    }

    public List<String> getDisciplineByCentreNiveauSession(String centre, String niveau, Long session) {
        List<ListeFinaleCandidatsCGS> candidats = listeFinaleCndidatsCGSRepository.findDisciplinesByCentreAndNiveauAndSession(centre, niveau, session);

        return candidats.stream()
                .map(ListeFinaleCandidatsCGS::getDiscipline)
                .distinct()
                .toList();
    }

    public List<ListeFinaleCandidatsCGS> repartitionParCentreCGS(Long session)
    {
        listeFinaleCndidatsCGSRepository.deleteAll();

        // 🔹 récupérer tous les candidats de la session
        List<ConcoursGeneral> candidats = concoursGeneralRepository.findBySessionAndDecision(session, 1);

        //log.info(String.valueOf(candidats.size()));

        // 🔹 récupérer les règles de répartition
        List<RegleCentre> regles = regleCentreRepository.findBySession(session);

        //log.info(String.valueOf(regles.size()));
        //log.info(String.valueOf(regles));

        List<ListeFinaleCandidatsCGS> resultats = new ArrayList<>();

        log.info("Etape Entree");

        try
        {
            for (ConcoursGeneral candidat : candidats)
            {
                log.info("Etape Entree A");
                //log.info(String.valueOf(candidat));
                for (RegleCentre regle : regles)
                {
                    log.info("Etape Entree B");
                    boolean match1 = false;
                    boolean match2 = false;
                    boolean match3 = false;
                    boolean match4 = false;

                    // ===== provenance ville =====
                    if (regle.getProvenanceVille() != null
                            && candidat.getEtablissement().getCentreExamen() != null
                            && regle.getProvenanceVille().contains(candidat.getEtablissement().getCentreExamen().getName()))
                    {

                        match1 = true;
                    }

                    // ===== provenance académie =====
                    if (regle.getProvenanceAcademie() != null
                            && (regle.getProvenanceAcademie().getCode().equals(candidat.getEtablissement().getInspectionAcademie().getCode())))
                    {
                        match2 = true;
                    }

                    // ===== discipline =====
                    if (regle.getDiscipline() != null
                            && regle.getDiscipline().contains(candidat.getSpecialite()))
                    {
                        match3 = true;
                    }

                    // ===== classe =====
                    if (regle.getClasses() != null
                            && regle.getClasses().contains(candidat.getLevel()))
                    {
                        match4 = true;
                    }


                    //log.info(String.valueOf(match1 + " " + match2 + " " + match3 + " " + match4));
                    if ((match1 || match2) && (match3 && match4))
                    {
                        // 🔹 choisir un centre de composition
                        Etablissement centre = regle.getCentreDeComposition();

                        ListeFinaleCandidatsCGS dto = new ListeFinaleCandidatsCGS();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                        dto.setDiscipline(candidat.getSpecialite());
                        dto.setFirstname(candidat.getFirstname());
                        dto.setLastname(candidat.getLastname());
                        dto.setSexe(candidat.getGender().name());
                        dto.setDate_birth(String.valueOf(candidat.getDate_birth()));
                        dto.setPlace_birth(candidat.getPlace_birth());
                        dto.setSerie(candidat.getSerie().getCode());
                        dto.setEtablissementOrigine(candidat.getEtablissement().getName());
                        dto.setCentreComposition(centre.getName());
                        dto.setAcademia(centre.getInspectionAcademie().getName());
                        dto.setLevel(candidat.getLevel());
                        dto.setSession(candidat.getSession());

                        resultats.add(dto);

                        break;
                    }
                }

            }
            listeFinaleCndidatsCGSRepository.saveAll(resultats);
            log.info("Etape Sortie");

        }
        catch (Exception e)
        {
            System.out.println("Erreur répartition CGS : " + e.getMessage());
            e.printStackTrace();
        }

        // 🔹 tri alphabétique
        return resultats.stream()
                .sorted(Comparator.comparing(ListeFinaleCandidatsCGS::getLastname))
                .toList();
    }


    // 🔹 Créer une règle
    public RegleCentre create(RegleCentre regle) {
        return regleCentreRepository.save(regle);
    }

    // 🔹 Lire toutes les règles
    public List<RegleCentre> findAll() {
        return regleCentreRepository.findAll();
    }

    // 🔹 Lire par id
    public Optional<RegleCentre> findById(String id) {
        return regleCentreRepository.findById(id);
    }

    // 🔹 Mettre à jour
    public RegleCentre update(String id, RegleCentre updated) {
        return regleCentreRepository.findById(id)
                .map(r -> {
                    r.setSession(updated.getSession());
                    r.setProvenanceVille(updated.getProvenanceVille());
                    r.setProvenanceAcademie(updated.getProvenanceAcademie());
                    r.setDiscipline(updated.getDiscipline());
                    r.setClasses(updated.getClasses());
                    r.setCentreDeComposition(updated.getCentreDeComposition());
                    return regleCentreRepository.save(r);
                })
                .orElseThrow(() -> new RuntimeException("Règle non trouvée"));
    }

    // 🔹 Supprimer
    public void delete(String id) {
        regleCentreRepository.deleteById(id);
    }

    public Map<String, List<ListeFinaleCandidatsCGS>> getAllData_()
    {
        List<ListeFinaleCandidatsCGS> allRepTirage = listeFinaleCndidatsCGSRepository.findAll();
        return allRepTirage
                .stream()
                .filter(p -> p.getAcademia() != null)
                .collect(Collectors.groupingBy(ListeFinaleCandidatsCGS::getAcademia));
    }

    public List<ListeFinaleCandidatsCGS> getAllData()
    {
        return listeFinaleCndidatsCGSRepository.findAll();
    }


    public List<ConcoursGeneral> getFilteredCandidatsForPdfReject(String etablissementId, Long session)
    {
        return concoursGeneralRepository.findByEtablissementIdAndSession(etablissementId, session);
    }


    public List<ConcoursGeneral> getCandidatsParEtablissement(String etablissementId, Long session) {
        return concoursGeneralRepository.findByEtablissementIdAndSession(etablissementId, session);
    }

    public List<String> getVillesOfCandidate(Long session)
    {
        return concoursGeneralRepository.findDistinctVilleNamesBySession(session);
    }

    public List<EtabSummaryDTO> getSummarizeEtabNotReceptionned(Long session)
    {
        return concoursGeneralRepository.findEtablissementsWithDecisionZero(session);
    }


    public List<ListeFinaleCandidatsCGS> getAlllCGS(Long session)
    {
        return listeFinaleCndidatsCGSRepository.findBySessionOrderByDisciplineAscLevelAsc(session);
    }

}