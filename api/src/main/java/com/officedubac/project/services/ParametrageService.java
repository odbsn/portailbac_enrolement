package com.officedubac.project.services;

import com.officedubac.project.dto.*;
import com.officedubac.project.models.*;
import com.officedubac.project.models.InspectionAcademie;
import com.officedubac.project.repository.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParametrageService
{
    @Autowired
    private final TypeCandidatRepository typeCandidatRepository;
    @Autowired
    private final TypeEtablissementRepository typeEtablissementRepository;
    @Autowired
    private final TypeEnseignementRepository typeEnseignementRepository;
    @Autowired
    private final EtablissementRepository etablissementRepository;
    @Autowired
    private final InspectionAcademieRepository inspectionAcademieRepository;
    @Autowired
    private final MatiereRepository matiereRepository;
    @Autowired
    private final SerieRepository serieRepository;
    @Autowired
    private final CentreEtatCivilRepository centreEtatCivilRepository;
    @Autowired
    private final NationalityRepository nationalityRepository;
    @Autowired
    private final OptionRepository optionRepository;
    @Autowired
    private final SpecialiteRepository specialiteRepository;
    @Autowired
    private final SujetRepository sujetRepository;
    @Autowired
    private final ProgrammationRepository programmationRepository;
    @Autowired
    private final RegionRepository regionRepository;
    @Autowired
    private final DepartementRepository departementRepository;
    @Autowired
    private final VilleRepository villeRepository;
    @Autowired
    private final UniversiteRepository universiteRepository;
    @Autowired
    private final ProfilRepository profilRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private CentreExamenRepository centreExamenRepository;
    @Autowired
    private final TypeFiliereRepository typeFiliereRepository;
    @Autowired
    private final TypeSerieRepository typeSerieRepository;
    @Autowired
    private final ActeursRepository acteursRepository;
    @Autowired
    private final CandidatRepository candidatRepository;
    @Autowired
    private final CompteDroitInscriptionRepository compteDroitInscriptionRepository;
    @Autowired
    private final EtatDeVersementRepository etatDeVersementRepository;
    @Autowired
    private final CandidatIsolatedRepository candidatIsolatedRepository;

    @Autowired
    private final DroitsInscriptionRepository droitsInscriptionRepository;


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private EmailService emailservice;

    public String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num); // entier
                } else {
                    return String.valueOf(num); // décimal
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue(); // plus sûr que cell.getCellFormula()
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
            case _NONE:
            case ERROR:
            default:
                return null;
        }
    }


    public TypeCandidat createTypeCandidat(TypeCandidatDTO typeCandidatDTO)
    {
        TypeCandidat type_cdt = TypeCandidat.builder()
                .name(typeCandidatDTO.getName())
                .build();

        return typeCandidatRepository.save(type_cdt);
    }

    public Etablissement createEtablissement(EtablissementDTO etablissementDTO)
    {
        Etablissement etab = Etablissement.builder()
                .name(etablissementDTO.getName())
                .code(etablissementDTO.getCode())
                .capacity(etablissementDTO.getCapacity())
                .nb_of_jury(etablissementDTO.getNb_of_jury())
                .capacity_eps(etablissementDTO.getCapacity_eps())
                .nb_act_sur_site(etablissementDTO.getNb_act_sur_site())
                .can_have_cdt(etablissementDTO.isCan_have_cdt())
                .etb_with_actor(etablissementDTO.isEtb_with_actor())
                .etb_was_ce(etablissementDTO.isEtb_was_ce())
                .etb_with_other_actor(etablissementDTO.isEtb_with_other_actor())
                .etb_is_ce(etablissementDTO.is_ce())
                .etab_have_cdt(etablissementDTO.isEtab_have_cdt())
                .ce_for_other(etablissementDTO.isCe_for_other())
                .inspectionAcademie(etablissementDTO.getInsp_aca())
                .departement(etablissementDTO.getDep())
                .ville(etablissementDTO.getVille())
                .centreExamen(etablissementDTO.getCentre_exam())
                .typeCandidat(etablissementDTO.getType_cdts())
                .typeEtablissement(etablissementDTO.getType_etab())
                .typeEnseignement(etablissementDTO.getType_ens())
                .zone(etablissementDTO.getZone())
                .build();

        return etablissementRepository.save(etab);
    }

    public Etablissement updateEtab(String idEtab, EtablissementDTO etablissementDTO, Long session)
    {
        // Lorsque l'etablissement est mis à jour, il doit y avoir une persistance sur l'ensemble de ses acolytes
        Etablissement etab = etablissementRepository.findById(idEtab).orElse(null);
        Etablissement newEtab;
        if (etab != null)
        {
            etab.setName(etablissementDTO.getName());
            etab.setCode(etablissementDTO.getCode());
            etab.setCapacity(etablissementDTO.getCapacity());
            etab.setNb_of_jury(etablissementDTO.getNb_of_jury());
            etab.setCapacity_eps(etablissementDTO.getCapacity_eps());
            etab.setNb_act_sur_site(etablissementDTO.getNb_act_sur_site());
            etab.setCan_have_cdt(etablissementDTO.isCan_have_cdt());
            etab.setEtb_with_actor(etablissementDTO.isEtb_with_actor());
            etab.setEtb_was_ce(etablissementDTO.isEtb_was_ce());
            etab.setEtb_with_other_actor(etablissementDTO.isEtb_with_other_actor());
            etab.setEtb_is_ce(etablissementDTO.is_ce());
            etab.setEtab_have_cdt(etablissementDTO.isEtab_have_cdt());
            etab.setCe_for_other(etablissementDTO.isCe_for_other());
            etab.setInspectionAcademie(etablissementDTO.getInsp_aca());
            etab.setDepartement(etablissementDTO.getDep());
            etab.setVille(etablissementDTO.getVille());
            etab.setCentreExamen(etablissementDTO.getCentre_exam());
            etab.setTypeCandidat(etablissementDTO.getType_cdts());
            etab.setTypeEtablissement(etablissementDTO.getType_etab());
            etab.setTypeEnseignement(etablissementDTO.getType_ens());
            etab.setZone(etablissementDTO.getZone());
            newEtab = etablissementRepository.save(etab);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Prog with ID " + idEtab + " is not found");
        }

        // Récupération des données
        List<Candidat> cdts = candidatRepository.findByEtablissementIdAndSession(idEtab, session);
        List<User> users = userRepository.findByActeur_EtablissementId(idEtab);
        List<EtatDeVersement> etatVers = etatDeVersementRepository.findByEtablissementIdAndSession(idEtab, session, Sort.by(Sort.Direction.DESC, "date_deposit"));
        CompteDroitsInscription cptDI = compteDroitInscriptionRepository.findByEtablissementIdAndSession(idEtab, session);
        List<Sujet> sujets = sujetRepository.findByEtablissementIdAndSession(idEtab, session);
        List<CandidateIsolated> cdtIs = candidatIsolatedRepository.findByEtablissementIdAndSession(idEtab, session);

        // Fonction utilitaire générique pour mettre à jour un champ Etablissement
        Consumer<Object> updateEtab = obj -> {
            try {
                var method = obj.getClass().getMethod("setEtablissement", newEtab.getClass());
                method.invoke(obj, newEtab);
            } catch (Exception ignored) {}
        };

        // Mise à jour listes en une seule instruction
        Optional.ofNullable(cdts)
                .ifPresent(list -> {
                    list.forEach(updateEtab);
                    candidatRepository.saveAll(list);
                });

        Optional.ofNullable(users)
                .ifPresent(list -> {
                    list.forEach(u -> {
                        if (u.getActeur() != null)
                            u.getActeur().setEtablissement(newEtab);
                    });
                    userRepository.saveAll(list);
                });

        Optional.ofNullable(etatVers)
                .ifPresent(list -> {
                    list.forEach(updateEtab);
                    etatDeVersementRepository.saveAll(list);
                });

        Optional.ofNullable(sujets)
                .ifPresent(list -> {
                    list.forEach(updateEtab);
                    sujetRepository.saveAll(list);
                });

        Optional.ofNullable(cdtIs)
                .ifPresent(list -> {
                    list.forEach(updateEtab);
                    candidatIsolatedRepository.saveAll(list);
                });

        // Cas particulier des objets simples
        Optional.ofNullable(cptDI)
                .ifPresent(cpt -> {
                    cpt.setEtablissement(newEtab);
                    compteDroitInscriptionRepository.save(cpt);
                });

        return newEtab;

    }

    public List<Etablissement> getEtablissement()
    {
        return etablissementRepository.findAll();
    }

    public List<TypeCandidat> getTypeCdts()
    {
        return typeCandidatRepository.findAll();
    }
    public List<TypeEnseignement> getTypeEns()
    {
        return typeEnseignementRepository.findAll();
    }
    public List<TypeEtablissement> getTypeEtab()
    {
        return typeEtablissementRepository.findAll();
    }

    public InspectionAcademie createIA(InspectionAcademieDTO inspectionAcademieDTO)
    {
        InspectionAcademie ia = InspectionAcademie.builder()
                .name(inspectionAcademieDTO.getName())
                .code(inspectionAcademieDTO.getCode())
                .region(inspectionAcademieDTO.getRegion())
                .build();

        return inspectionAcademieRepository.save(ia);
    }

    public List<InspectionAcademie> getIAs()
    {
        return inspectionAcademieRepository.findAll();
    }

    public Universite createUniv(UniversiteDTO universiteDTO)
    {
        Universite univ = Universite.builder()
                .name(universiteDTO.getName())
                .code(universiteDTO.getCode())
                .region(universiteDTO.getRegion())
                .build();

        return universiteRepository.save(univ);
    }

    public List<Universite> getUnivs()
    {
        return universiteRepository.findAll();
    }


    public Serie createSerie(SerieDTO serieDTO)
    {
        Serie sr = Serie.builder()
                .name(serieDTO.getName())
                .code(serieDTO.getCode())
                .typeFiliere(serieDTO.getType_filiere())
                .typeSerie(serieDTO.getType_serie())
                .build();

        return serieRepository.save(sr);
    }

    public Serie updateSerie(String idS, SerieDTO serieDTO)
    {
        Serie update_serie = serieRepository.findById(idS).orElse(null);

        if (update_serie != null)
        {
            update_serie.setName(serieDTO.getName());
            update_serie.setCode(serieDTO.getCode());
            update_serie.setTypeFiliere(serieDTO.getType_filiere());
            update_serie.setTypeSerie(serieDTO.getType_serie());
            return serieRepository.save(update_serie);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Prog with ID " + idS + " is not found");
        }
    }

    public List<Serie> getSerie()
    {
        return serieRepository.findAll();
    }

    public List<TypeFiliere> getTypeFilieres()
    {
        return typeFiliereRepository.findAll();
    }

    public List<TypeSerie> getTypeSeries()
    {
        return typeSerieRepository.findAll();
    }

    public Map<String, List<Serie>> getSeriesGroupedByTypeFiliere() {
        List<Serie> allSeries = serieRepository.findAll();

        return allSeries.stream()
                .filter(s -> s.getTypeFiliere() != null && s.getTypeFiliere().getName() != null)
                .collect(Collectors.groupingBy(s -> s.getTypeFiliere().getName()));
    }

    public Map<String, List<Matiere>> getMatieresGroupedByTypeMatiere() {
        List<Matiere> allMatieres = matiereRepository.findAll();

        return allMatieres.stream()
                .filter(s -> s.getTypeMatiere() != null && s.getTypeMatiere().getName() != null)
                .collect(Collectors.groupingBy(s -> s.getTypeMatiere().getName()));
    }

    public Map<String, List<Etablissement>> getEtabsGroupedByIA() {
        List<Etablissement> allEtabs = etablissementRepository.findAll();

        return allEtabs.stream()
                .filter(s -> s.getInspectionAcademie() != null && s.getInspectionAcademie().getName() != null)
                .collect(Collectors.groupingBy(s -> s.getInspectionAcademie().getName()));
    }

    public Map<String, List<InspectionAcademie>> getIAGroupedByRegions() {
        List<InspectionAcademie> allIAs = inspectionAcademieRepository.findAll();

        return allIAs.stream()
                .filter(s -> s.getRegion() != null && s.getRegion().getName() != null)
                .collect(Collectors.groupingBy(s -> s.getRegion().getName()));
    }

    public Map<String, List<CentreEtatCivil>> getCECGroupedByDep() {
        List<CentreEtatCivil> allIAs = centreEtatCivilRepository.findAll();

        return allIAs.stream()
                .filter(s -> s.getDepartement() != null && s.getDepartement().getName() != null)
                .collect(Collectors.groupingBy(s -> s.getDepartement().getName()));
    }

    public Matiere createMatiere(MatiereDTO matiereDTO)
    {
        Matiere mt = Matiere.builder()
                .name(matiereDTO.getName())
                .code(matiereDTO.getCode())
                .coef_princ(matiereDTO.getCoef_princ())
                .coef_prat(matiereDTO.getCoef_prat())
                .serie(matiereDTO.getSerie())
                .build();
        return matiereRepository.save(mt);
    }

    public Matiere updateMatiere(String idM, MatiereDTO matiereDTO)
    {
        Matiere update_mat = matiereRepository.findById(idM).orElse(null);

        if (update_mat != null)
        {
            update_mat.setName(matiereDTO.getName());
            update_mat.setCode(matiereDTO.getCode());
            update_mat.setCoef_princ(matiereDTO.getCoef_princ());
            update_mat.setCoef_prat(matiereDTO.getCoef_prat());
            update_mat.setSerie(matiereDTO.getSerie());
            return matiereRepository.save(update_mat);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Data with ID " + idM + " is not found");
        }
    }

    public List<Matiere> getMatieres()
    {
        return matiereRepository.findAll();
    }

    public List<Nationality> getNationality()
    {
        List<Nationality> nats = nationalityRepository.findAll();
        return nats;
    }

    public List<Option> getOptions()
    {
        List<Option> options = optionRepository.findAll();
        return options;
    }

    public List<Specialite> getSpecialites()
    {
        List<Specialite> specialites = specialiteRepository.findAll();
        return specialites;
    }




    /***
     * @auteur : Mansour DIOUF
     * Ce service me permet de récupérer les matières d'une série
     */
    public List<Matiere> getMatiereFromSerie(String serieId)
    {
        System.out.println("Serie Id"+serieId);
        List<Matiere> mat = matiereRepository.findBySerie_Id(serieId);
        return mat;
    }

    public Programmation createProg(ProgrammationDTO programmationDTO)
    {
        Programmation prg = Programmation.builder()
                .edition(programmationDTO.getEdition())
                .date_start(programmationDTO.getDate_start())
                .date_end(programmationDTO.getDate_end())
                .bfem_IfEPI(programmationDTO.getBfemEPI())
                .bfem_IfI(programmationDTO.getBfemI())
                .codeSup1(programmationDTO.getCodeSup1())
                .codeSup2(programmationDTO.getCodeSup2())
                .publicKey(programmationDTO.getPublicKey())
                .secretKey(programmationDTO.getSecretKey())
                .build();

        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("edition").is(prg.getEdition())
        ));

        List<Programmation> existing = mongoTemplate.find(query, Programmation.class);
        log.info("OK = {}", existing);

        if (!existing.isEmpty())
        {
            for (Programmation p : existing) {
                if (p.getEdition() == programmationDTO.getEdition()) {
                    throw new BusinessResourceException(
                            "login-error",
                            "Attention, cette edition du BAC existe déjà !",
                            HttpStatus.CONFLICT
                    );
                }
            }
        }

        if (prg.getDate_start().isAfter(prg.getDate_end()))
        {
            throw new BusinessResourceException(
                    "year-error",
                    "La date de début semble postérieure à la date de fin des enrôlements. Merci de vérifier les dates pour plus de cohérence !",
                    HttpStatus.NOT_ACCEPTABLE
            );
        }


        return programmationRepository.save(prg);
    }

    public Programmation updateProg(String idPrg, ProgrammationDTO programmationDTO)
    {
        Programmation update_prg = programmationRepository.findById(idPrg).orElse(null);

        if (update_prg != null)
        {
            update_prg.setDate_start(programmationDTO.getDate_start());
            update_prg.setDate_end(programmationDTO.getDate_end());
            update_prg.setCodeSup1(programmationDTO.getCodeSup1());
            update_prg.setCodeSup2(programmationDTO.getCodeSup2());
            update_prg.setPublicKey(programmationDTO.getPublicKey());
            update_prg.setSecretKey(programmationDTO.getSecretKey());
            return programmationRepository.save(update_prg);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Prog with ID " + idPrg + " is not found");
        }
    }

    public List<Programmation> getProgs()
    {
        return programmationRepository.findAll();
    }

    public Programmation getDerniereProg() {
        return programmationRepository.findTopByOrderByIdDesc();
    }


    public Region createRegion(RegionDTO regionDTO)
    {
        Region rg = Region.builder()
                .name(regionDTO.getName())
                .build();

        return regionRepository.save(rg);
    }

    public Region updateRegion(String idR, RegionDTO regionDTO)
    {
        Region rg = regionRepository.findById(idR).orElse(null);
        if (rg != null)
        {
            rg.setName(regionDTO.getName());
            return regionRepository.save(rg);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Data with ID " + idR + " is not found");
        }
    }

    public List<Region> getRegions()
    {
        return regionRepository.findAll();
    }

    public Departement createDepartement(DepartementDTO departementDTO)
    {
        Departement dp = Departement.builder()
                .name(departementDTO.getName())
                .region(departementDTO.getRegion())
                .build();

        return departementRepository.save(dp);
    }
    public Departement updateDep(String idD, DepartementDTO departementDTO)
    {
        Departement dep = departementRepository.findById(idD).orElse(null);

        if (dep != null)
        {
            dep.setName(departementDTO.getName());
            dep.setRegion(departementDTO.getRegion());
            return departementRepository.save(dep);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Data with ID " + idD + " is not found");
        }
    }

    public List<Departement> getDepartements()
    {
        return departementRepository.findAll();
    }

    public Ville createVille(VilleDTO villeDTO)
    {
        Ville vl = Ville.builder()
                .name(villeDTO.getName())
                .departement(villeDTO.getDepartement())
                .build();
        return villeRepository.save(vl);
    }

    public Ville updateVl(String idV, VilleDTO villeDTO)
    {
        Ville vl = villeRepository.findById(idV).orElse(null);

        if (vl != null)
        {
            vl.setName(villeDTO.getName());
            vl.setDepartement(villeDTO.getDepartement());
            return villeRepository.save(vl);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Data with ID " + idV + " is not found");
        }
    }

    public List<Ville> getVilles()
    {
        return villeRepository.findAll();
    }

    public CentreExamen createCExam(CentreExamenDTO centreExamenDTO)
    {
        CentreExamen cex = CentreExamen.builder()
                .name(centreExamenDTO.getName())
                .ville(centreExamenDTO.getVille())
                .build();
        return centreExamenRepository.save(cex);
    }

    public CentreExamen updateCEx(String idCEx, CentreExamenDTO centreExamenDTO)
    {
        CentreExamen cExam = centreExamenRepository.findById(idCEx).orElse(null);

        if (cExam != null)
        {
            cExam.setName(centreExamenDTO.getName());
            cExam.setVille(centreExamenDTO.getVille());
            return centreExamenRepository.save(cExam);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Data with ID " + idCEx + " is not found");
        }
    }

    public List<CentreExamen> getCExams()
    {
        return centreExamenRepository.findAll();
    }

    public CentreEtatCivil createEtatCivil(CentreEtatCivilDTO centreEtatCivilDTO)
    {
        CentreEtatCivil cec = CentreEtatCivil.builder()
                .name(centreEtatCivilDTO.getName())
                .code(centreEtatCivilDTO.getCode())
                .departement(centreEtatCivilDTO.getDepartement())
                .build();

        return centreEtatCivilRepository.save(cec);
    }

    public CentreEtatCivil updateCEC(String idCec, CentreEtatCivilDTO centreEtatCivilDTO)
    {
        CentreEtatCivil cEtc = centreEtatCivilRepository.findById(idCec).orElse(null);

        if (cEtc != null)
        {
            cEtc.setName(centreEtatCivilDTO.getName());
            cEtc.setCode(centreEtatCivilDTO.getCode());
            cEtc.setDepartement(centreEtatCivilDTO.getDepartement());
            return centreEtatCivilRepository.save(cEtc);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Data with ID " + idCec + " is not found");
        }
    }

    public List<CentreEtatCivil> getCEC()
    {
        List<CentreEtatCivil> cecs = centreEtatCivilRepository.findAll();
        return cecs;
    }

    public Profil createProfil(ProfilDTO profilDTO)
    {
        Profil prf = Profil.builder()
                .name(Role.valueOf(profilDTO.getName()))
                .build();

        return profilRepository.save(prf);
    }

    public User createUser(UserDTO userDTO, boolean send_access_smtp) throws MessagingException
    {
        log.info(String.valueOf(send_access_smtp));
        Map<String, Object> variables = new HashMap<>();
        Profil prf = profilRepository.findByName(userDTO.getProfil().getName().name());
        User user = User.builder()
                .firstname(userDTO.getFirstname())
                .lastname(userDTO.getLastname())
                .phone(userDTO.getPhone())
                .email(userDTO.getEmail())
                .login(userDTO.getLogin())
                .first_connexion(true)
                .password(new BCryptPasswordEncoder().encode(userDTO.getPassword()))
                .acteur(userDTO.getActeur())
                .profil(prf)
                .state_account(userDTO.isState_account())
                .build();

        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("email").is(user.getEmail()),
                Criteria.where("phone").is(user.getPhone()),
                Criteria.where("login").is(user.getLogin())
        ));

        List<User> existing = mongoTemplate.find(query, User.class);
        log.info("OK = {}", existing);

        if (!existing.isEmpty())
        {
            for (User u : existing) {
                if (u.getLogin().equals(userDTO.getLogin())) {
                    throw new BusinessResourceException(
                            "login-error",
                            "Attention, ce login existe déjà !",
                            HttpStatus.CONFLICT
                    );
                }
                if (u.getEmail().equals(userDTO.getEmail())) {
                    throw new BusinessResourceException(
                            "email-error",
                            "Attention, cette adresse email existe déjà !",
                            HttpStatus.CONFLICT
                    );
                }
                if (u.getPhone().equals(userDTO.getPhone())) {
                    throw new BusinessResourceException(
                            "phone-error",
                            "Attention, ce numéro de téléphone existe déjà !",
                            HttpStatus.CONFLICT
                    );
                }
            }
        }

        if (send_access_smtp)
        {
            variables.put("title", "Bienvenue sur PortailBAC \uD83C\uDF89 !");
            variables.put("message", "Merci de vous être inscrit, le compte est activé avec succés.");
            variables.put("login", userDTO.getLogin());
            variables.put("password", userDTO.getPassword());
            emailservice.sendEmailAccountCreated(userDTO.getEmail(), "[Office du Baccalauréat / PortailBAC] Création officielle de compte", variables);

        }

        return userRepository.save(user);

    }

    public boolean importUserByFile(String filePath) {
        boolean allSent = false;
        try (InputStream file = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(file))
        {

            Sheet sheet = workbook.getSheetAt(0);
            List<User> batchList = new ArrayList<>();
            List<UserMailDTO> mailList = new ArrayList<>(); // pour stocker email + login + mot de passe en clair

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // ignorer les en-têtes

                Cell emailCell = row.getCell(0);
                if (emailCell == null || emailCell.getCellType() == CellType.BLANK) continue;
                String email = getCellValue(emailCell);
                if (email == null || email.isEmpty()) continue;

                // Vérification et récupération de l'établissement
                Cell etabCell = row.getCell(1); // supposons que le code établissement est dans la 2ème colonne
                if (etabCell == null || etabCell.getCellType() == CellType.BLANK) continue;

                // Vérification et récupération de l'établissement
                Cell callCell = row.getCell(2); // supposons que le code établissement est dans la 2ème colonne
                if (callCell == null || callCell.getCellType() == CellType.BLANK) continue;
                String phone = getCellValue(callCell);

                // Génération login et mot de passe
                String login = getCellValue(etabCell);
                //String passwordPlain = "officedubac";
                String passwordPlain = CodeGenerator.generateCode();
                String passwordHashed = new BCryptPasswordEncoder().encode(passwordPlain);

                Etablissement etab = etablissementRepository.findByCode(getCellValue(etabCell));
                if (etab == null) continue;

                Acteurs act = new Acteurs();
                act.setEtablissement(etab);
                Acteurs acteur = acteursRepository.save(act);

                Profil profil = profilRepository.findByName("AGENT_DE_SAISIE");
                if (profil == null) continue;

                // Création de l'utilisateur
                User us = new User();
                us.setEmail(email);
                us.setPhone(phone);
                us.setLogin(getCellValue(etabCell));
                us.setPassword(passwordHashed);
                us.setActeur(acteur);
                us.setProfil(profil);
                us.setState_account(true);
                us.setFirst_connexion(true);
                batchList.add(us);

                // Stocker les infos pour l'email
                mailList.add(new UserMailDTO(email, login, passwordPlain));
            }

            // Sauvegarder tous les utilisateurs en batch
            userRepository.saveAll(batchList);
            CompletableFuture<Boolean> future = emailservice.sendAccountCreatedEmails(mailList, "[Office du Baccalauréat / PortailBAC] Création officielle de compte");

            // Bloque jusqu'à ce que tous les emails soient envoyés
            allSent = future.get();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return allSent;
    }


    public User updateUser(String idUsr, UserDTO userDTO)
    {
        Profil prf = profilRepository.findByName(userDTO.getProfil().getName().name());
        User update_usr = userRepository.findById(idUsr).orElse(null);

        if (update_usr != null)
        {
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("_id").ne(idUsr), // exclure le candidat en cours
                    new Criteria().orOperator(
                            Criteria.where("email").is(userDTO.getEmail()),
                            Criteria.where("phone").is(userDTO.getPhone()),
                            Criteria.where("login").is(userDTO.getLogin())
                    )
            ));

            List<User> existing = mongoTemplate.find(query, User.class);
            log.info("OK = {}", existing);

            if (!existing.isEmpty())
            {
                for (User u : existing) {
                    if (u.getLogin().equals(userDTO.getLogin())) {
                        throw new BusinessResourceException(
                                "login-error",
                                "Attention, ce login existe déjà !",
                                HttpStatus.CONFLICT
                        );
                    }
                    if (u.getEmail().equals(userDTO.getEmail())) {
                        throw new BusinessResourceException(
                                "email-error",
                                "Attention, cette adresse email existe déjà !",
                                HttpStatus.CONFLICT
                        );
                    }
                    if (u.getPhone().equals(userDTO.getPhone())) {
                        throw new BusinessResourceException(
                                "phone-error",
                                "Attention, ce numéro de téléphone existe déjà !",
                                HttpStatus.CONFLICT
                        );
                    }
                }
            }
            update_usr.setFirstname(userDTO.getFirstname());
            update_usr.setLastname(userDTO.getLastname());
            update_usr.setPhone(userDTO.getPhone());
            update_usr.setEmail(userDTO.getEmail());
            update_usr.setLogin(userDTO.getLogin());
            update_usr.setActeur(userDTO.getActeur());
            update_usr.setProfil(prf);
            update_usr.setState_account(userDTO.isState_account());
            return userRepository.save(update_usr);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Prog with ID " + idUsr + " is not found");
        }
    }

    public List<User> getUsers()
    {
        return userRepository.findAll();
    }

    public Map<String, List<User>> getUserGroupedByProfil()
    {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(p -> p.getProfil() != null)
                .collect(Collectors.groupingBy(s -> s.getProfil().getName().name()));
    }

    public void deleteUser(String idUsr)
    {
        try
        {
            User delete_usr = userRepository.findById(idUsr).orElse(null);

            if (delete_usr != null)
            {
                userRepository.delete(delete_usr);
            }
            else
            {
                // Handle the case where the user is not found
                throw new NotFoundException("User with ID " + idUsr + " is not found");
            }
        }
        catch (Exception e)
        {
            System.out.println("Error message: " + e.getMessage());
        }
    }

    public boolean updateStatus(String idUsr, boolean state)
    {
        User update_usr = userRepository.findById(idUsr).orElse(null);

        if (update_usr != null)
        {
            if (update_usr.isState_account())
            {
                update_usr.setState_account(!state);
            }
            else
            {
                update_usr.setState_account(state);
            }

            User u = userRepository.save(update_usr);
            return u.isState_account();
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Prog with ID " + idUsr + " is not found");
        }
    }

    // Nombre total d'utilisateurs
    public long getTotalUsers() {
        Query query = new Query();
        query.addCriteria(Criteria.where("profil.name").is("AGENT_DE_SAISIE"));
        return mongoTemplate.count(query, User.class);
    }

    // Liste des utilisateurs dont first_connexion = false
    public long countFirstConnexionFalseUsersAgent() {
        Query query = new Query();
        query.addCriteria(Criteria.where("first_connexion").is(false));
        query.addCriteria(Criteria.where("profil.name").is("AGENT_DE_SAISIE"));
        return mongoTemplate.count(query, User.class);
    }

    public boolean isValid(String apiKey, String apiSecret) {
        return programmationRepository
                .findByPublicKeyAndSecretKey(apiKey, apiSecret)
                .isPresent();
    }

    public List<User> getUserReceptionniste()
    {
        return userRepository.findByProfil_Name(Role.RECEPTIONNISTE);
    }

    public CompteDroitsInscription updateMandataire(String mdt, MandataireDTO mandataireDTO)
    {
        CompteDroitsInscription cptDI = compteDroitInscriptionRepository.findById(mdt).orElse(null);

        if (cptDI != null)
        {
            cptDI.setRepresentative(mandataireDTO.getRepresentative());
            cptDI.setPhone(mandataireDTO.getPhone());
            return compteDroitInscriptionRepository.save(cptDI);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("CDI with ID " + mdt + " is not found");
        }
    }


}
