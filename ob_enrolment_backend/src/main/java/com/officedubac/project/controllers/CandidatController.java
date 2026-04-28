package com.officedubac.project.controllers;

import com.officedubac.project.dto.*;
import com.officedubac.project.models.*;
import com.officedubac.project.repository.CandidatRepository;
import com.officedubac.project.services.CandidatService;
import com.officedubac.project.services.ParametrageService;
import com.officedubac.project.utils.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/enrollment-candidats")
@RequiredArgsConstructor
@Tag(name="Candidat Controller", description = "Endpoints responsables de l'enrôlement des candidats")
public class CandidatController
{
    @Autowired
    private final ParametrageService parametrageService;
    @Autowired
    private final CandidatService candidatService;
    @Autowired
    private final CandidatRepository candidatRepository;
    @Autowired
    private IpUtils ipUtils;

    @Operation(summary="Service d'ajout d'un candidat")
    @PostMapping(value="/add-candidat")
    public ResponseEntity<BaseMorte> postCandidat(@RequestBody CandidatDTO candidatDTO) throws Exception {

        return ResponseEntity.ok(this.candidatService.createCandidat(candidatDTO));
    }

    @Operation(summary="Service de mise à jour d'un candidat")
    @PutMapping(value="/update-candidat")
    public ResponseEntity<Candidat> updateCandidat(@RequestParam String idCdt,
                                                   @RequestBody CandidatDTO candidatDTO,
                                                   HttpServletRequest request) throws Exception {
        String clientIp = ipUtils.getClientIp(request);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(this.candidatService.updateCandidat(idCdt, candidatDTO, login, clientIp));
    }

    @Operation(summary="Service de suppression d'un candidat")
    @DeleteMapping(value = "/delete-cdt")
    public ResponseEntity<Void> decisionCandidat(@RequestParam String idCdt, @RequestParam String login) throws Exception {
        this.candidatService.deleteCandidat(idCdt, login);
        return ResponseEntity.ok().build();
    }

    @Operation(summary="Service de suppression d'un candidat")
    @DeleteMapping(value = "/delete-isolate-cdt")
    public ResponseEntity<Void> decisionCandidat_(@RequestParam String idCdt, @RequestParam String login) throws Exception {
        this.candidatService.deleteIsolateCandidat(idCdt, login);
        return ResponseEntity.ok().build();
    }

    @Operation(summary="Service de listing des candidats")
    @GetMapping(value = "/found-cdts")
    public ResponseEntity<List<Candidat>> getCandidats() throws Exception {
        return ResponseEntity.ok(this.candidatService.getCandidats());
    }

    @Operation(summary="Service de recherche d'un candidat")
    @GetMapping(value = "/search-cdt")
    public ResponseEntity<Candidat> getCandidat(@RequestParam int extrait) throws Exception {
        return ResponseEntity.ok(this.candidatService.getCandidat(extrait));
    }

    @Operation(summary="Service de listing des centres d'examen")
    @GetMapping(value="/centres-examen")
    public ResponseEntity<List<CentreExamen>> getCExam() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getCExams());
    }

    @Operation(summary="Service de recherche des candidats d'un sujet")
    @PostMapping(value = "/found-cdt-by-subject/{etablissementId}/{session}")
    public ResponseEntity<List<Candidat>> getCandidatsBySubject(@PathVariable String etablissementId, @PathVariable Long session, @RequestParam String sujet) throws Exception {
        return ResponseEntity.ok(this.candidatService.getCandidatsBySubject(etablissementId, session, sujet));
    }

    @Operation(summary="Service de recherche d'un centre d'etat civil")
    @PostMapping(value = "/found-cec")
    public ResponseEntity<CentreEtatCivil> getCEC(@RequestParam String nameCEC) throws Exception {
        return ResponseEntity.ok(this.candidatService.getCECByName(nameCEC));
    }

    @Operation(summary="Service de création d'un sujet")
    @PostMapping("/create-sujet")
    public ResponseEntity<Sujet> createSujet(@RequestBody SujetDTO sujetDTO)
    {
        return ResponseEntity.ok(this.candidatService.createSujet(sujetDTO));
    }

    @Operation(summary="Service de mis à jour d'un sujet")
    @PutMapping("/update-sujet")
    public ResponseEntity<?> updateSujet(@RequestParam String idS, @RequestBody SujetDTO sujetDTO) throws Exception {

        return ResponseEntity.ok(this.candidatService.updateSujet(idS, sujetDTO));
    }

    @Operation(summary="Service de suppression d'un sujet avec les affectations")
    @DeleteMapping("/delete-sujet/{id}")
    public ResponseEntity<String> deleteSujet(@PathVariable String id) {
        String result = candidatService.deleteSujet(id);

        return switch (result) {
            case "OK" -> ResponseEntity.ok("Sujet supprimé avec succès");
            case "NotFound" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sujet introuvable");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression");
        };
    }

    @Operation(summary="Service de listing des sujets")
    @GetMapping(value = "/sujets")
    public ResponseEntity<List<Sujet>> getSeries() throws Exception {
        return ResponseEntity.ok(this.candidatService.getSujets());
    }

    @Operation(summary="Service de listing des sujets selon l'établissement et l'édition du BAC")
    @GetMapping("/sujets/etablissement/{etablissementId}/{session}")
    public ResponseEntity<List<Sujet>> getSujetsParEtablissement(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getSujetsParEtablissement(etablissementId, session));
    }

    @Operation(summary="Service de listing des candidats selon l'établissement et l'édition du BAC")
    @GetMapping("/candidats/etablissement/{etablissementId}/{session}")
    public ResponseEntity<List<Candidat>> getCandidatsParEtablissement(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getCandidatsParEtablissement(etablissementId, session));
    }

    @Operation(summary="Service de listing des candidats selon l'établissement et l'édition du BAC et regroupés par série")
    @GetMapping("/candidats/{etablissementId}/{session}")
    public ResponseEntity<Map<String, List<Candidat>>> getCdtsGroupedBySerie(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getCandidatsGroupedBySerie(etablissementId, session));
    }

    @Operation(summary="Service de listing des candidats selon l'établissement et l'édition du BAC et regroupés par sujet")
    @GetMapping("/candidats-sujets/{etablissementId}/{session}")
    public ResponseEntity<Map<String, List<Candidat>>> getCandidatsGroupedBySujet(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getCandidatsGroupedBySujet(etablissementId, session));
    }

    @Operation(summary="Service d'affection d'un sujet à des candidats")
    @PutMapping("/assign-sujet-to-candidats")
    public ResponseEntity<Void> assignSubjectToCandidat(@RequestBody SujetAndCandidatDTO sujetAndCandidatDTO) throws Exception {
        candidatService.assignSubjectToCandidate(sujetAndCandidatDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary="Service d'ajout d'un candidat au CGS")
    @PostMapping(value="/add-candidat-cgs")
    public ResponseEntity<ConcoursGeneral> postCandidatCGS(@RequestBody ConcoursGeneralDTO concoursGeneralDTO) throws Exception {

        return ResponseEntity.ok(this.candidatService.createConcoursGeneral(concoursGeneralDTO));
    }

    @Operation(summary="Service de mis à jour des infos d'un candidat au CGS")
    @PutMapping(value="/update-candidat-cgs")
    public ResponseEntity<ConcoursGeneral> updateCandidat(@RequestParam String idCgs,
                                                   @RequestBody ConcoursGeneralDTO concoursGeneralDTO) throws Exception
    {
        return ResponseEntity.ok(this.candidatService.updateConcoursGeneral(idCgs, concoursGeneralDTO));
    }

    @Operation(summary="Service de mis à jour des infos d'un candidat au CGS")
    @PutMapping(value="/update-candidat-cgs-reception")
    public ResponseEntity<ConcoursGeneral> updateCandidat_(@RequestParam String idCgs,
                                                          @RequestBody ConcoursGeneralDTO concoursGeneralDTO) throws Exception
    {
        return ResponseEntity.ok(this.candidatService.updateConcoursGeneral_(idCgs, concoursGeneralDTO));
    }

    @Operation(summary="Service de suppression d'un candidat du CGS")
    @DeleteMapping(value = "/delete-cgs")
    public ResponseEntity<Void> deleteCgs(@RequestParam String idCdt) throws Exception {
        this.candidatService.deleteCGS(idCdt);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count-dispo-cgs")
    public ResponseEntity<Long> countCandidats(
            @RequestParam String specialite,
            @RequestParam String classe,
            @RequestParam Integer session,
            @RequestParam String etablissement) {

        long count = candidatService.countCandidats(specialite, classe, session, etablissement);
        return ResponseEntity.ok(count);
    }

    /**
    @Operation(summary="Service d'affection d'une spécialité à des candidats pour le CGS")
    @PutMapping("/assign-specialite-to-candidats-cgs")
    public ResponseEntity<Void> assignSpecialiteToCgs(@RequestBody SpecialiteAndCgsDTO specialiteAndCgsDTO) throws Exception
    {
        candidatService.assignSpecialiteToCgs(specialiteAndCgsDTO);
        return ResponseEntity.ok().build();
    }*/

    @Operation(summary="Service de recherche des candidats d'un sujet")
    @PostMapping(value = "/found-cdtcgs-by-specialite")
    public ResponseEntity<List<ConcoursGeneral>> getCdtCgsBySpecialite(@RequestParam String level, @RequestParam String specialite) throws Exception {
        return ResponseEntity.ok(this.candidatService.getCdtCgsBySpecialite(level, specialite));
    }

    @Operation(summary="Service de listing des spécialités du CGS")
    @GetMapping("/cgs/listeSpecialite")
    public ResponseEntity<List<SpecialiteCGS>> getAllSpecialite() throws Exception {
        return ResponseEntity.ok(this.candidatService.getAllSpecialite());
    }

    @Operation(summary="Service de listing des candidats du CGS selon l'établissement et l'édition")
    @GetMapping("/candidats-cgs/{etablissementId}/{session}")
    public ResponseEntity<List<ConcoursGeneral>> getCdtsCgsParEtablissement(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getCdtsCgsParEtablissement(etablissementId, session));
    }

    @Operation(summary="Service de listing des candidats du CGS selon l'établissement et l'édition du BAC et regroupés par série")
    @GetMapping("/candidats-cgs/grouped-by-classe/{etablissementId}/{session}")
    public ResponseEntity<Map<String, List<ConcoursGeneral>>> getCdtCgsGroupedByClasse(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getCdtCgsGroupedByClasse(etablissementId, session));
    }

    @Operation(summary="Service de listing des séries")
    @GetMapping(value="/series")
    public ResponseEntity<List<Serie>> series() throws Exception {

        return ResponseEntity.ok(this.candidatService.getSerie());
    }

    @Operation(summary="Service de listing des centres d'etat civil")
    @GetMapping(value="/centre-etat-civil")
    public ResponseEntity<List<CentreEtatCivil>> centreEC() throws Exception {

        return ResponseEntity.ok(this.candidatService.getCEC());
    }

    @Operation(summary="Service de listing des nationalités")
    @GetMapping(value="/nationality")
    public ResponseEntity<List<Nationality>> getNationality() throws Exception {

        return ResponseEntity.ok(this.candidatService.getNationality());
    }

    @Operation(summary="Service de listing des options")
    @GetMapping(value="/options")
    public ResponseEntity<List<Option>> getOptions() throws Exception {

        return ResponseEntity.ok(this.candidatService.getOptions());
    }

    @Operation(summary="Service de listing des spécialités")
    @GetMapping(value="/specialites")
    public ResponseEntity<List<Specialite>> getSpecialites() throws Exception {

        return ResponseEntity.ok(this.candidatService.getSpecialites());
    }

    @Operation(summary="Service de listing des établissements")
    @GetMapping(value="/etablissements")
    public ResponseEntity<List<Etablissement>> getEtablissements() throws Exception {

        return ResponseEntity.ok(this.candidatService.getEtablissement());
    }

    @Operation(summary="Service de recupération de l'édition actuelle du BAC")
    @GetMapping(value="/programmation-last")
    public ResponseEntity<Programmation> getLastProgrammation() throws Exception {

        return ResponseEntity.ok(this.candidatService.getDerniereProg());
    }

    @Operation(summary="Service de listing des matières par série")
    @GetMapping(value="/matiere-by-serie")
    public ResponseEntity<List<Matiere>> postCandidat(@RequestParam String serie_id) throws Exception {

        return ResponseEntity.ok(this.candidatService.getMatiereFromSerie(serie_id));
    }

    @Operation(summary="Service de listing des motifs de rejet")
    @GetMapping(value="/rejets")
    public ResponseEntity<List<Rejet>> allRejet() throws Exception {

        return ResponseEntity.ok(this.candidatService.getRejets());
    }


    @GetMapping("/stat-series-etab/{etablissementId}/session/{session}")
    public ResponseEntity<List<StatsDTO>> getCandidatsParSerie(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getNombreCandidatsParSerie(etablissementId, session));
    }

    @GetMapping("/stat-sexe-etab/{etablissementId}/session/{session}")
    public ResponseEntity<List<StatsDTO>> getNombreCandidatsParSexe(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getNombreCandidatsParSexe(etablissementId, session));
    }

    @GetMapping("/stat-eps-etab/{etablissementId}/session/{session}")
    public ResponseEntity<List<StatsDTO>> getNombreCandidatsParEPS(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getNombreCandidatsParEPS(etablissementId, session));
    }

    @GetMapping("/stat-handicap-etab/{etablissementId}/session/{session}")
    public ResponseEntity<List<StatsDTO>> getNombreCandidatsParHandicap(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getNombreCandidatsParHandicap(etablissementId, session));
    }

    @GetMapping("/stat-epFac-etab/{etablissementId}/session/{session}")
    public ResponseEntity<List<StatsDTO>> getNombreCandidatsParEF(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getNombreCandidatsParEF(etablissementId, session));
    }

    @GetMapping("/stat-options-etab/{etablissementId}/session/{session}")
    public ResponseEntity<List<StatsDTO>> getNombreCandidatsParOptions(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getNombreCandidatsParOptions(etablissementId, session));
    }

    @Operation(summary="Service de recupération des droits FAEB")
    @GetMapping("/compte-droits-inscription")
    public ResponseEntity<CompteDroitsInscription> compte_droits_inscription(@RequestParam String establishmentId, @RequestParam Long session)
    {
        CompteDroitsInscription result = candidatService.getCompteDroitsInscription(establishmentId, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de recupération du nombre EPF")
    @GetMapping("/decompte-nombre-epFac")
    public ResponseEntity<Map<String, Long>> epreuveFac(@RequestParam String establishmentId, @RequestParam Long session)
    {
        Map<String, Long> result = candidatService.compterFacultatives(establishmentId, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de vérification d'un doublon la même année")
    @GetMapping("/doublon/{yearRegistryNum}/{cec}/{session}")
    public ResponseEntity<Candidat> doublon(@PathVariable int yearRegistryNum, @RequestParam String registryNum, @PathVariable String cec, @PathVariable long session)
    {
        Candidat result = candidatService.checkDoublon(yearRegistryNum, registryNum, cec, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de vérification d'un doublon selon le numéro de téléphone")
    @GetMapping("/doublon-by-tel/{phone1}/{session}")
    public ResponseEntity<Candidat> doublonByTel(@PathVariable String phone1, @PathVariable long session)
    {
        Candidat result = candidatService.checkDoublonNumTel(phone1, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de vérification d'un doublon selon l'email")
    @GetMapping("/doublon-by-email/{email}/{session}")
    public ResponseEntity<Candidat> doublonByEmail(@PathVariable String email, @PathVariable long session)
    {
        Candidat result = candidatService.checkDoublonEmail(email, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de recupération d'un dossier de candidature via le numéro")
    @PostMapping("/checkByDosNumber/{dosNumber}/{session}/{etablissementId}")
    public ResponseEntity<Candidat> checkByDosNumber(@PathVariable String dosNumber, @PathVariable Long session, @PathVariable String etablissementId)
    {
        return ResponseEntity.ok(this.candidatService.checkByDosNumber(dosNumber, session, etablissementId));
    }

    @GetMapping("/get-candidats/{page}/{size}")
    public ResponseEntity<?> getArchives(
            @PathVariable int page,
            @PathVariable int size,
            @RequestParam long session
    ) {
        Page<Candidat> p = this.candidatService.getAllCdtsBySession(page, size, session);

        Map<String, Object> res = new HashMap<>();
        res.put("content", p.getContent());
        res.put("totalElements", p.getTotalElements());
        res.put("totalPages", p.getTotalPages());
        res.put("size", p.getSize());
        res.put("page", p.getNumber());

        return ResponseEntity.ok(res);
    }

    @Operation(summary="Service des centres d'examen pour un I")
    @GetMapping("/centre-exam-for-I")
    public ResponseEntity<List<CentreExamen>> centreExamByEtabAndSession(@RequestParam String establishmentId, @RequestParam Integer session)
    {
        List<CentreExamen> result = candidatService.getCentresExamenByEtablissement(establishmentId, session);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/etablissements-cgs/{session}")
    public ResponseEntity<List<Etablissement>> getNombreCandidatsParHandicap(@PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.candidatService.getEtablissementsFromCandidats(session));
    }




}
