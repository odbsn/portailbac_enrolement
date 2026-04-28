package com.officedubac.project.controllers;

import com.officedubac.project.dto.*;
import com.officedubac.project.models.*;
import com.officedubac.project.services.AuthenticationService;
import com.officedubac.project.services.ParametrageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/parametrage")
@RequiredArgsConstructor
@Tag(name="Paramétrage Controller", description = "Endpoints responsables de la gestion des paramétres de la plateforme")
public class ParametrageController
{
    @Autowired
    private final ParametrageService parametrageService;
    @Autowired
    private final AuthenticationService authenticationService;

    @Operation(summary="Service de création d'un compte")
    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody SignUpDTO signUpDTO)
    {
        return ResponseEntity.ok(authenticationService.signup(signUpDTO));
    }

    @Operation(summary="Service de mis à jour du mot de passe")
    @PutMapping("/update-password")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> updatePassword(@RequestParam String email) throws MessagingException {
        return ResponseEntity.ok(authenticationService.updatePassword(email));
    }


    @Operation(summary="Service de changement du mot de passe")
    @PutMapping("/changed-password")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> updatePassword(@RequestParam String usrId, @RequestBody ChangedPasswordDTO changedPasswordDTO)
    {
        return ResponseEntity.ok(authenticationService.changedPassword(usrId, changedPasswordDTO));
    }

    @Operation(summary="Service de création d'un utilisateur")
    @PostMapping("/create-user")
    public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO, @RequestParam boolean send_access_smtp) throws MessagingException {
        return ResponseEntity.ok(parametrageService.createUser(userDTO, send_access_smtp));
    }

    @Operation(summary="Service de mis à jour d'un utilisateur")
    @PutMapping(value="/update-user")
    public ResponseEntity<User> updateCandidat(@RequestParam String idUsr, @RequestBody UserDTO userDTO) throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.updateUser(idUsr, userDTO));
    }

    @Operation(summary="Service de suppression d'un utilisateur")
    @DeleteMapping(value = "/delete-user")
    public ResponseEntity<Void> deleteUser(@RequestParam String idUsr) throws Exception {
        this.parametrageService.deleteUser(idUsr);
        return ResponseEntity.ok().build();
    }

    @Operation(summary="Service de mis à jour du statut d'un compte d'utilisateur")
    @PatchMapping(value="/update-account")
    public ResponseEntity<Boolean> updateState(@RequestParam String idUsr, @RequestParam boolean state) throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.updateStatus(idUsr, state));
    }

    @Operation(summary="Service de listing des utilisateurs")
    @GetMapping(value="/users")
    public ResponseEntity<Map<String, List<User>>> users() throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.getUserGroupedByProfil());
    }

    @Operation(summary="Service de récupération des matières par série")
    @GetMapping(value="/matiere-by-serie")
    public ResponseEntity<List<Matiere>> postCandidat(@RequestParam String serie_id) throws Exception {

        return ResponseEntity.ok(this.parametrageService.getMatiereFromSerie(serie_id));
    }

    @Operation(summary="Service de récupération des nationalités")
    @GetMapping(value="/nationality")
    public ResponseEntity<List<Nationality>> getNationality() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getNationality());
    }

    @Operation(summary="Service de récupération des options")
    @GetMapping(value="/options")
    public ResponseEntity<List<Option>> getOptions() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getOptions());
    }

    @Operation(summary="Service de récupération des spécialités")
    @GetMapping(value="/specialites")
    public ResponseEntity<List<Specialite>> getSpecialites() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getSpecialites());
    }

    @Operation(summary="Service de création d'un programme d'enrôlement")
    @PostMapping("/create-prog")
    public ResponseEntity<Programmation> createProg(@RequestBody ProgrammationDTO programmationDTO)
    {
        return ResponseEntity.ok(parametrageService.createProg(programmationDTO));
    }

    @Operation(summary="Service de mis à jour d'un programme d'enrôlement")
    @PutMapping(value="/update-prog")
    public ResponseEntity<Programmation> updateProg(@RequestParam String idPrg, @RequestBody ProgrammationDTO programmationDTO) throws Exception {

        return ResponseEntity.ok(this.parametrageService.updateProg(idPrg, programmationDTO));
    }

    @Operation(summary="Service de listing des programmations")
    @GetMapping(value="/programmations")
    public ResponseEntity<List<Programmation>> getProgrammations() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getProgs());
    }

    @Operation(summary="Service de listing d'une programmation en cours")
    @GetMapping(value="/programmation-last")
    public ResponseEntity<Programmation> getLastProgrammation() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getDerniereProg());
    }

    ///

    @Operation(summary="Service de création d'une région")
    @PostMapping("/create-region")
    public ResponseEntity<Region> createReg(@RequestBody RegionDTO regionDTO)
    {
        return ResponseEntity.ok(parametrageService.createRegion(regionDTO));
    }

    @Operation(summary="Service de mis à jour d'une région")
    @PutMapping(value="/update-region")
    public ResponseEntity<Region> updateReg(@RequestParam String idR, @RequestBody RegionDTO regionDTO) throws Exception {

        return ResponseEntity.ok(this.parametrageService.updateRegion(idR, regionDTO));
    }

    @Operation(summary="Service de listing des régions")
    @GetMapping(value="/regions")
    public ResponseEntity<List<Region>> getRegions() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getRegions());
    }

    @Operation(summary="Service de création d'un département")
    @PostMapping("/create-dep")
    public ResponseEntity<Departement> createDep(@RequestBody DepartementDTO departementDTO)
    {
        return ResponseEntity.ok(parametrageService.createDepartement(departementDTO));
    }

    @Operation(summary="Service de mis à jour d'un département")
    @PutMapping(value="/update-departement")
    public ResponseEntity<Departement> updateDep(@RequestParam String idD, @RequestBody DepartementDTO departementDTO) throws Exception {

        return ResponseEntity.ok(this.parametrageService.updateDep(idD, departementDTO));
    }

    @Operation(summary="Service de listing d'un département")
    @GetMapping(value="/departements")
    public ResponseEntity<List<Departement>> getDep() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getDepartements());
    }

    @Operation(summary="Service de création d'une ville")
    @PostMapping("/create-ville")
    public ResponseEntity<Ville> createVil(@RequestBody VilleDTO villeDTO)
    {
        return ResponseEntity.ok(parametrageService.createVille(villeDTO));
    }

    @Operation(summary="Service de mis à jour d'une ville")
    @PutMapping(value="/update-ville")
    public ResponseEntity<Ville> updateVille(@RequestParam String idV, @RequestBody VilleDTO villeDTO) throws Exception {

        return ResponseEntity.ok(this.parametrageService.updateVl(idV, villeDTO));
    }

    @Operation(summary="Service de listing des villes")
    @GetMapping(value="/villes")
    public ResponseEntity<List<Ville>> getVil() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getVilles());
    }

    @Operation(summary="Service de création d'un centre d'examen")
    @PostMapping("/create-centre-examen")
    public ResponseEntity<CentreExamen> createCExam(@RequestBody CentreExamenDTO centreExamenDTO)
    {
        return ResponseEntity.ok(parametrageService.createCExam(centreExamenDTO));
    }

    @Operation(summary="Service de mis à jour d'un centre d'examen")
    @PutMapping(value="/update-centre-examen")
    public ResponseEntity<CentreExamen> updateCExam(@RequestParam String idCex, @RequestBody CentreExamenDTO centreExamenDTO) throws Exception {

        return ResponseEntity.ok(this.parametrageService.updateCEx(idCex, centreExamenDTO));
    }

    @Operation(summary="Service de listing des centres d'examen")
    @GetMapping(value="/centres-examen")
    public ResponseEntity<List<CentreExamen>> getCExam() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getCExams());
    }

    @Operation(summary="Service de listing des types de filières")
    @GetMapping(value="/type-filiere")
    public ResponseEntity<List<TypeFiliere>> getTypeFiliere() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getTypeFilieres());
    }

    @Operation(summary="Service de listing des types de séries")
    @GetMapping(value="/type-series")
    public ResponseEntity<List<TypeSerie>> getTypeSerie() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getTypeSeries());
    }

    @Operation(summary="Service de création des centres d'etat civil")
    @PostMapping("/create-cec")
    public ResponseEntity<CentreEtatCivil> createCEC(@RequestBody CentreEtatCivilDTO centreEtatCivilDTO)
    {
        return ResponseEntity.ok(parametrageService.createEtatCivil(centreEtatCivilDTO));
    }

    @Operation(summary="Service de mis à jour des centres d'etat civil")
    @PutMapping(value="/update-cec")
    public ResponseEntity<CentreEtatCivil> updateCEtatCiv(@RequestParam String idCeC, @RequestBody CentreEtatCivilDTO centreEtatCivilDTO) throws Exception {

        return ResponseEntity.ok(this.parametrageService.updateCEC(idCeC, centreEtatCivilDTO));
    }

    @Operation(summary="Service de listing des centres d'etat civil")
    @GetMapping(value="/centre-etat-civil")
    public ResponseEntity<List<CentreEtatCivil>> centreEC() throws Exception {

        return ResponseEntity.ok(this.parametrageService.getCEC());
    }

    @Operation(summary="Service de création des séries")
    @PostMapping("/create-serie")
    public ResponseEntity<Serie> createSerie(@RequestBody SerieDTO serieDTO)
    {
        return ResponseEntity.ok(parametrageService.createSerie(serieDTO));
    }

    @Operation(summary="Service de mis à jour des séries")
    @PutMapping(value="/update-serie")
    public ResponseEntity<Serie> updateSerie(@RequestParam String idSerie, @RequestBody SerieDTO serieDTO) throws Exception {

        return ResponseEntity.ok(this.parametrageService.updateSerie(idSerie, serieDTO));
    }

    @Operation(summary="Service de listing des séries")
    @GetMapping(value="/series")
    public ResponseEntity<List<Serie>> series() throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.getSerie());
    }

    @Operation(summary="Service de listing et de regroupement des séries par filière")
    @GetMapping("/grouped-by-filiere")
    public ResponseEntity<Map<String, List<Serie>>> getGroupedByFiliere() throws Exception {
        return ResponseEntity.ok(this.parametrageService.getSeriesGroupedByTypeFiliere());
    }

    @Operation(summary="Service de listing et de regroupement des matières par type de matière")
    @GetMapping("/grouped-by-type-matiere")
    public ResponseEntity<Map<String, List<Matiere>>> getGroupedByTypeMatiere() throws Exception {
        return ResponseEntity.ok(this.parametrageService.getMatieresGroupedByTypeMatiere());
    }

    @Operation(summary="Service de listing et de regroupement des établissements par académie")
    @GetMapping("/etab-grouped-by-aca")
    public ResponseEntity<Map<String, List<Etablissement>>> getEtabGroupedByIA() throws Exception {
        return ResponseEntity.ok(this.parametrageService.getEtabsGroupedByIA());
    }

    @Operation(summary="Service de listing et de regroupement des académie par région")
    @GetMapping("/ia-grouped-by-reg")
    public ResponseEntity<Map<String, List<InspectionAcademie>>> getIAGroupedByRegion() throws Exception {
        return ResponseEntity.ok(this.parametrageService.getIAGroupedByRegions());
    }

    @Operation(summary="Service de listing et de regroupement des centres d'etat civil par département")
    @GetMapping("/cec-grouped-by-dep")
    public ResponseEntity<Map<String, List<CentreEtatCivil>>> getCECGroupedByDep() throws Exception {
        return ResponseEntity.ok(this.parametrageService.getCECGroupedByDep());
    }

    @Operation(summary="Service de création des matières")
    @PostMapping("/create-matiere")
    public ResponseEntity<Matiere> createMatiere(@RequestBody MatiereDTO matiereDTO)
    {
        return ResponseEntity.ok(parametrageService.createMatiere(matiereDTO));
    }

    @Operation(summary="Service de mis à jour des matières")
    @PutMapping(value="/update-matiere")
    public ResponseEntity<Matiere> updateMatiere(@RequestParam String idMat, @RequestBody MatiereDTO matiereDTO) throws Exception {

        return ResponseEntity.ok(this.parametrageService.updateMatiere(idMat, matiereDTO));
    }

    @Operation(summary="Service de listing des matières")
    @GetMapping(value="/matieres")
    public ResponseEntity<List<Matiere>> matieres() throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.getMatieres());
    }

    @Operation(summary="Service de création des établissements")
    @PostMapping("/create-etablissement")
    public ResponseEntity<Etablissement> createEtab(@RequestBody EtablissementDTO etablissementDTO)
    {
        return ResponseEntity.ok(parametrageService.createEtablissement(etablissementDTO));
    }

    @Operation(summary="Service de mis à jour des établissements")
    @PutMapping(value="/update-etablissement")
    public ResponseEntity<Etablissement> updateEtabl(@RequestParam String idEt, @RequestParam Long session, @RequestBody EtablissementDTO etabDTO) throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.updateEtab(idEt, etabDTO, session));
    }

    @Operation(summary="Service de listing des établissements")
    @GetMapping(value="/etablissements")
    public ResponseEntity<List<Etablissement>> etabs() throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.getEtablissement());
    }

    @Operation(summary="Service de listing des types de candidats")
    @GetMapping(value="/type-candidats")
    public ResponseEntity<List<TypeCandidat>> getTypeCdts() throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.getTypeCdts());
    }

    @Operation(summary="Service de listing des types d'enseignements")
    @GetMapping(value="/type-enseignements")
    public ResponseEntity<List<TypeEnseignement>> getTypeEns() throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.getTypeEns());
    }

    @Operation(summary="Service de listing des types d'établissements")
    @GetMapping(value="/type-etablissements")
    public ResponseEntity<List<TypeEtablissement>> getTypeEtab() throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.getTypeEtab());
    }

    @Operation(summary="Service de création des académies")
    @PostMapping("/create-IA")
    public ResponseEntity<InspectionAcademie> createIA(@RequestBody InspectionAcademieDTO inspectionAcademieDTO)
    {
        return ResponseEntity.ok(parametrageService.createIA(inspectionAcademieDTO));
    }

    @Operation(summary="Service de listing des académies")
    @GetMapping(value="/inspection-academies")
    public ResponseEntity<List<InspectionAcademie>> ias() throws Exception
    {
        return ResponseEntity.ok(this.parametrageService.getIAs());
    }

    @GetMapping("/users/stats")
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", parametrageService.getTotalUsers());
        stats.put("UsersConnected", parametrageService.countFirstConnexionFalseUsersAgent());
        return stats;
    }






}
