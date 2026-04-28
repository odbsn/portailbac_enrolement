package com.officedubac.project.controllers;

import com.officedubac.project.dto.*;
import com.officedubac.project.models.*;
import com.officedubac.project.services.AuditService;
import com.officedubac.project.services.CandidatService;
import com.officedubac.project.services.ParametrageService;
import com.officedubac.project.services.StatsService;
import com.officedubac.project.utils.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/validation-candidats")
@RequiredArgsConstructor
@Tag(name="Validation Controller", description = "Endpoints responsables de la validation des dossiers de candidature")
public class ValidationController
{

    @Autowired
    private final CandidatService candidatService;
    @Autowired
    private final AuditService auditService;
    @Autowired
    private final ParametrageService parametrageService;
    @Autowired
    private final StatsService statsService;

    @Autowired
    private IpUtils ipUtils;

    @Operation(summary="Service de filtrage des candidats selon l'établissement et la série")
    @GetMapping("/candidats/filter")
    public ResponseEntity<List<Candidat>> filterCandidats(
            @RequestParam String etablissementId,
            @RequestParam Long session
    ) {
        List<Candidat> result = candidatService.getFilteredCandidats(etablissementId, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de filtrage des candidats isolés selon l'établissement et la série")
    @GetMapping("/candidats/isolate")
    public ResponseEntity<List<CandidateIsolated>> isolateCandidats(
            @RequestParam String etablissementId,
            @RequestParam Long session
    ) {
        List<CandidateIsolated> result = candidatService.getIsolatedCandidats(etablissementId, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de mis à jour des coupons numériques achetés")
    @PatchMapping(value="/update-coupons-etab")
    public ResponseEntity<?> updateCouponsNombres(@RequestParam String idEV, @RequestParam String f, @RequestParam String l, @RequestBody VignetteAddDTO vignetteAddDTO) throws Exception {

        return ResponseEntity.ok(this.candidatService.updateEV(idEV, vignetteAddDTO, f, l));
    }

    @Operation(summary="Service de rejet d'une mauvaise vignette")
    @PatchMapping(value="/reject-quittance")
    public ResponseEntity<?> rejectVignette(@RequestParam String idEV, @RequestParam boolean rejet) throws Exception {

        return ResponseEntity.ok(this.candidatService.rejectFile(idEV, rejet));
    }

    @Operation(summary="Service de mis à jour des coupons numériques achetés")
    @PatchMapping(value="/correction-coupons-etab")
    public ResponseEntity<?> correctionCouponsNombres(@RequestParam String idEV, @RequestParam String motif, @RequestParam String f, @RequestParam String l) throws Exception {

        return ResponseEntity.ok(this.candidatService.updateEV_(idEV, motif, f, l));
    }

    @Operation(summary="Service de mis à jour de la décision d'un dossier de candidature")
    @PatchMapping(value="/update-decision-cdt")
    public ResponseEntity<Candidat> updateDecision(@RequestParam String idCdt, @RequestBody CandidatDecisionDTO candidatDecisionDTO, HttpServletRequest request) throws Exception
    {
        String clientIp = ipUtils.getClientIp(request);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(this.candidatService.updateDecision(idCdt, candidatDecisionDTO, login, clientIp));
    }

    @Operation(summary="Service de filtrage des états de versement")
    @GetMapping("/etat-versements/filter")
    public ResponseEntity<List<EtatDeVersement>> filterEVs(@RequestParam String etablissementId, @RequestParam Long session)
    {
        List<EtatDeVersement> result = candidatService.getFilteredEVs(etablissementId, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de filtrage des états de versement")
    @GetMapping("/etat-versements")
    public ResponseEntity<List<EtatDeVersement>> filterEVs(@RequestParam Long session)
    {
        List<EtatDeVersement> result = candidatService.getFilteredEVs_(session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de listing des éditions du BAC")
    @GetMapping(value="/programmations")
    public ResponseEntity<List<Programmation>> getProgrammations() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getProgs());
    }

    @Operation(summary="Service de recupération des droits FAEB")
    @GetMapping("/compte-droits-inscription")
    public ResponseEntity<CompteDroitsInscription> compte_droits_inscription(@RequestParam String establishmentId, @RequestParam Long session)
    {
        CompteDroitsInscription result = candidatService.getCompteDroitsInscription(establishmentId, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de recupération des mandataires depuis le compte")
    @GetMapping("/get-mandataires")
    public ResponseEntity<List<CompteDroitsInscription>> get_mandataires(@RequestParam Long session)
    {
        List<CompteDroitsInscription> result = candidatService.getMandataires(session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de recupération des comptes FEAB")
    @GetMapping("/get-all-faeb")
    public ResponseEntity<List<CompteDroitsInscription>> get_all_faeb(@RequestParam Long session)
    {
        List<CompteDroitsInscription> result = candidatService.getFaeb(session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de recupération du nombre EPF")
    @GetMapping("/decompte-nombre-epFac")
    public ResponseEntity<Map<String, Long>> epreuveFac(@RequestParam String establishmentId, @RequestParam Long session)
    {
        Map<String, Long> result = candidatService.compterFacultatives(establishmentId, session);
        return ResponseEntity.ok(result);
    }

    @Operation(summary="Service de mise à jour du mandataire et d'autorisation des receptions")
    @PatchMapping(value="/autorisation-reception")
    public ResponseEntity<CompteDroitsInscription> updateAutorisation(@RequestParam String idCmptDroitInsc,
                                                   @RequestBody AutorisationReception autorisationReception) throws Exception
    {
        return ResponseEntity.ok(this.candidatService.enabledReception(idCmptDroitInsc, autorisationReception));
    }

    @Operation(summary="Boite noire")
    @GetMapping("/get-audit-reception-dosssier/{id}")
    public ResponseEntity<List<AuditLog>> getLogsByCandidate(@PathVariable("id") String candidateId) {
        List<AuditLog> logs = auditService.getLogsByCandidateId(candidateId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/operator-daily/{start}/{end}/{session}")
    public List<OperatorDailyCountDTO> getDailyStats(
            @PathVariable("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @PathVariable("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PathVariable("session") Integer session
    ) {
        return statsService.countDailyByOperator(start, end, session);
    }

    @GetMapping("/operations-reception/{session}/{ia}")
    public ResponseEntity<List<EtablissementSummaryReception>> getSummarizeOperations(@PathVariable long session, @PathVariable String ia) {
        return ResponseEntity.ok(candidatService.summarize(session, ia));
    }

    @GetMapping("/operations-reception-by-scolarite/{session}/{operator}")
    public ResponseEntity<List<EtablissementSummaryReceptionScolarite>> getSummarizeOperations_(@PathVariable long session, @PathVariable String operator) {
        return ResponseEntity.ok(candidatService.summarizeScolariteOps(session, operator));
    }

    @GetMapping("/etab-not-receptionned/{session}")
    public ResponseEntity<List<Object>> getEtabWithAgainReception_(@PathVariable long session) {
        return ResponseEntity.ok(candidatService.summarizeSchoolWithDossierEnAttente(session));
    }

    @GetMapping("/operations-reception-by-ops/{session}/{operator}")
    public ResponseEntity<List<Object>> getEtablissementReceivedByOps(@PathVariable int session, @PathVariable String operator) {
        return ResponseEntity.ok(candidatService.etablissementReceivedByOps(session, operator));
    }

    @GetMapping("/operators-by-etab/{session}/{etab}")
    public ResponseEntity<List<Document>> summarizeOperatorsByEtablissement(@PathVariable int session, @PathVariable String etab)
    {
        return ResponseEntity.ok(candidatService.summarizeOperatorsByEtablissement(session, etab));
    }

    @GetMapping("/all-operations-reception/{session}")
    public EtablissementSummaryReception_ getSummarizeOperations_(@PathVariable("session") Long session) {
        return candidatService.summarize_(session);
    }

    @GetMapping("/all-operations-reception-CGS/{session}")
    public EtablissementSummaryReception_ getSummarizeOperations_CGS(@PathVariable("session") Long session) {
        return candidatService.summarize_CGS(session);
    }

    @GetMapping("/all-receptionniste")
    public List<User> getReceptionniste() {
        return parametrageService.getUserReceptionniste();
    }

    @Operation(summary="Service de d'isolement d'un candidat")
    @DeleteMapping(value = "/cdt-isolated")
    public ResponseEntity<Void> isolateCandidate(@RequestParam String idCdt, @RequestParam String login) throws Exception {
        this.candidatService.isolatedCandidat(idCdt, login);
        return ResponseEntity.ok().build();
    }

    @Operation(summary="Service de réintégration d'un candidat")
    @DeleteMapping(value = "/cdt-reintegrated")
    public ResponseEntity<Void> integratedCandidate(@RequestParam String idCdt, @RequestParam String login) throws Exception {
        this.candidatService.reintegratedCandidat(idCdt, login);
        return ResponseEntity.ok().build();
    }

    @Operation(summary="Service de mis à jour d'un mandataire")
    @PutMapping("/update-mandataire")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CompteDroitsInscription> updateMandataire(@RequestParam String mdt, @RequestBody MandataireDTO mandataireDTO)
    {
        return ResponseEntity.ok(parametrageService.updateMandataire(mdt, mandataireDTO));
    }

    @GetMapping("/get-all-candidats-of-academia/{page}/{size}")
    public ResponseEntity<Map<String, Object>> getAllCandidatsOfAcademia(
            @PathVariable int page,
            @PathVariable int size,
            @RequestParam(required = false) Integer session,
            @RequestParam(required = false) String iaCode
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Candidat> result = candidatService.getAllCandidatsOfAcademia(pageable, session, iaCode);

        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getContent());
        response.put("total", result.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/serie-in-academia")
    public ResponseEntity<List<Serie>> getSerieInAcademia(
            @RequestParam String iaCode,
            @RequestParam Integer session
    ) {
        List<Serie> series = candidatService.getSeriesInAcademia(iaCode, session);
        return ResponseEntity.ok(series);
    }


    @GetMapping("/get-all-candidats-by-serie-and-sexe")
    public ResponseEntity<List<Candidat>> getAllCandidatsBySerieAndSexe(
            @RequestParam Long session,
            @RequestParam String iaCode,
            @RequestParam(required = false) String serieCode)
    {

        List<Candidat> response = candidatService.getAllCandidatsBySerieAndSexe(session, iaCode, serieCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-isolated-candidats-by-serie-and-sexe")
    public ResponseEntity<List<CandidateIsolated>> getAllIsolatedCandidatsBySerieAndSexe(
            @RequestParam Long session,
            @RequestParam String iaCode,
            @RequestParam(required = false) String serieCode)
    {

        List<CandidateIsolated> response = candidatService.getAllIsoCandidatsBySerieAndSexe(session, iaCode, serieCode);
        return ResponseEntity.ok(response);
    }
}
