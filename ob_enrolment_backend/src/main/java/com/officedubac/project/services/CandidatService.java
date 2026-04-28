package com.officedubac.project.services;

import org.bson.Document;
import com.officedubac.project.dto.*;
import com.officedubac.project.exception.ResourceAlreadyExists;
import com.officedubac.project.exception.TechnicalException;
import com.officedubac.project.models.*;
import com.officedubac.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

import javax.security.auth.Subject;
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
public class CandidatService
{
    @Autowired
    private final CandidatRepository candidatRepository;
    @Autowired
    private final CandidatToCampusenRepository candidatToCampusenRepository;
    @Autowired
    private final SujetRepository sujetRepository;
    @Autowired
    private final CentreEtatCivilRepository centreEtatCivilRepository;
    @Autowired
    private final EtablissementRepository etablissementRepository;
    @Autowired
    private final TypeCandidatRepository typeCandidatRepository;
    @Autowired
    private final SerieRepository serieRepository;
    @Autowired
    private final NationalityRepository nationalityRepository;
    @Autowired
    private final OptionRepository optionRepository;
    @Autowired
    private final SpecialiteRepository specialiteRepository;
    @Autowired
    private final MatiereRepository matiereRepository;
    @Autowired
    private final ProgrammationRepository programmationRepository;
    @Autowired
    private final RejetRepository rejetRepository;
    @Autowired
    private final EtatDeVersementRepository etatDeVersementRepository;
    @Autowired
    private final CompteDroitInscriptionRepository compteDroitInscriptionRepository;
    @Autowired
    private final ConcoursGeneralRepository concoursGeneralRepository;
    @Autowired
    private final SpecialiteCGSRepository specialiteCGSRepository;
    @Autowired
    private final CandidatIsolatedRepository isolatedCandidatRepository;
    @Autowired
    private final AuditService auditService;
    @Autowired
    private final ImportDataService importDataService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Serie> getSerie()
    {
        List<Serie> series = serieRepository.findAll();
        return series;
    }

    public List<CentreEtatCivil> getCEC()
    {
        List<CentreEtatCivil> cecs = centreEtatCivilRepository.findAll();
        return cecs;
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

    public List<Etablissement> getEtablissement()
    {
        List<Etablissement> etabs = etablissementRepository.findAll();
        return etabs;
    }

    public List<Matiere> getMatiereFromSerie(String serieId)
    {
        System.out.println("Serie Id"+serieId);
        List<Matiere> mat = matiereRepository.findBySerie_Id(serieId);
        return mat;
    }

    public List<Programmation> getProgs()
    {
        return programmationRepository.findAll();
    }

    public Programmation getDerniereProg() {
        return programmationRepository.findTopByOrderByIdDesc();
    }

    private boolean equalsIgnoreCaseAndAccent(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        String normA = Normalizer.normalize(a, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();

        String normB = Normalizer.normalize(b, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();

        return normA.equals(normB);
    }

    @Transactional
    public BaseMorte createCandidat(CandidatDTO candidatDTO)
    {
        // Formatage de la date de naissance
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dateNaissance = LocalDate.parse(candidatDTO.getDate_birth(), formatter);

        // Génération du numéro d’enrôlement et du code dossier
        String code = "NO" +
                candidatDTO.getCentreEtatCivil().getCode() +
                candidatDTO.getYear_registry_num() +
                candidatDTO.getRegistry_num() +
                candidatDTO.getSession();

        String dosNumberBySessionAndEtab = candidatDTO.getDosNumber() + "_" +
                candidatDTO.getSession() + "_" +
                (candidatDTO.getEtablissement() != null
                        ? candidatDTO.getEtablissement().getCode()
                        : "NULL");

        // Construction de l'entité candidat
        Candidat cdt = Candidat.builder()
                .dosNumber(candidatDTO.getDosNumber())
                .session(candidatDTO.getSession())
                .firstname(candidatDTO.getFirstname())
                .lastname(candidatDTO.getLastname())
                .date_birth(dateNaissance)
                .place_birth(candidatDTO.getPlace_birth())
                .gender(candidatDTO.getGender())
                .phone1(candidatDTO.getPhone1())
                .phone2(candidatDTO.getPhone2())
                .email(candidatDTO.getEmail())
                .centreEtatCivil(candidatDTO.getCentreEtatCivil())
                .centreExamen(candidatDTO.getCentreExamen())
                .year_registry_num(candidatDTO.getYear_registry_num())
                .registry_num(candidatDTO.getRegistry_num())
                .bac_do_count(candidatDTO.getBac_do_count())
                .year_bfem(candidatDTO.getYear_bfem())
                .subject(candidatDTO.getSubject())
                .handicap(candidatDTO.isHandicap())
                .type_handicap(candidatDTO.getType_handicap())
                .eps(candidatDTO.getEps())
                .cdt_is_cgs(candidatDTO.isCdt_is_cgs())
                .typeCandidat(candidatDTO.getTypeCandidat())
                .etablissement(candidatDTO.getEtablissement())
                .serie(candidatDTO.getSerie())
                .nationality(candidatDTO.getNationality())
                .matiere1(candidatDTO.getMatiere1())
                .matiere2(candidatDTO.getMatiere2())
                .matiere3(candidatDTO.getMatiere3())
                .countryBirth(candidatDTO.getCountryBirth())
                .eprFacListA(candidatDTO.getEprFacListA())
                .eprFacListB(candidatDTO.getEprFacListB())
                .origine_bfem(candidatDTO.getOrigine_bfem())
                .numEnrolement(code)
                .dosNumber_by_session_and_etablissement(dosNumberBySessionAndEtab)
                .alreadyBac(candidatDTO.isAlreadyBac())
                .codeEnrolementEC(candidatDTO.getCodeEnrolementEC())
                .decision(0)
                .build();

        // Vérification d’un éventuel redoublant
        BaseMorte bm = importDataService.checkRedoublantByEtatCivil(
                cdt.getCentreEtatCivil().getCode(),
                cdt.getYear_registry_num(),
                cdt.getRegistry_num()
        );

        log.info("Yeaahh" + cdt.isAlreadyBac());
        log.info("Yeaahh 2" + candidatDTO.isAlreadyBac());

        if (bm == null || (cdt.isAlreadyBac() && bm.getExclusionDuree() == 0) || (cdt.isAlreadyBac() && bm.getExclusionDuree() + 1 + bm.getExYearBac() <= cdt.getSession()))
        {
            // Aucun redoublant détecté
            log.info("{} {}", cdt.getYear_registry_num(), cdt.getDate_birth().getYear());

            Programmation prg = programmationRepository.findByEdition(cdt.getSession());

            // Vérifications d’unicité (numéro, dossier, téléphone, email)
            Query query = new Query();
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("numEnrolement").is(cdt.getNumEnrolement()),
                    Criteria.where("dosNumber_by_session_and_etablissement").is(cdt.getDosNumber_by_session_and_etablissement()),
                    Criteria.where("phone1").is(cdt.getPhone1()),
                    Criteria.where("email").is(cdt.getEmail())
            ));

            List<Candidat> existing = mongoTemplate.find(query, Candidat.class);
            log.info("Candidats existants = {}", existing);

            if (!existing.isEmpty()) {
                for (Candidat c : existing) {
                    if (c.getNumEnrolement().equals(cdt.getNumEnrolement())) {
                        throw new BusinessResourceException(
                                "numEnrolement-error",
                                "Vos références d'État civil ont déjà été utilisées",
                                HttpStatus.CONFLICT
                        );
                    }
                    if (c.getDosNumber_by_session_and_etablissement().equals(cdt.getDosNumber_by_session_and_etablissement())) {
                        throw new BusinessResourceException(
                                "dosNumber-error",
                                "Le numéro de dossier est déjà utilisé pour cette session et cet établissement",
                                HttpStatus.CONFLICT
                        );
                    }
                    if (c.getPhone1().equals(cdt.getPhone1())) {
                        throw new BusinessResourceException(
                                "phone1-error",
                                "Le numéro de téléphone est déjà utilisé",
                                HttpStatus.CONFLICT
                        );
                    }
                    if (c.getEmail().equals(cdt.getEmail())) {
                        throw new BusinessResourceException(
                                "email-error",
                                "L'adresse email est déjà utilisée",
                                HttpStatus.CONFLICT
                        );
                    }
                }
            }

            // Vérification cohérence date de naissance / année de déclaration
            int birthYear = cdt.getDate_birth().getYear();
            log.info("Résultat comparaison = {}", cdt.getYear_registry_num() < birthYear);

            if (cdt.getYear_registry_num() < birthYear) {
                throw new BusinessResourceException(
                        "year-error",
                        "L'année de déclaration ne doit pas être antérieure à celle de naissance",
                        HttpStatus.NOT_ACCEPTABLE
                );
            }

            // Vérifications sur l’année du BFEM selon le type d’établissement
            String typeEtab = cdt.getEtablissement().getTypeEtablissement().getCode();

            if ("EFI".equals(typeEtab) && cdt.getYear_bfem() > prg.getBfem_IfEPI()) {
                throw new BusinessResourceException(
                        "diff-year-bfem-error1",
                        "Pour ce candidat, l'année d'obtention du BFEM ne doit pas être supérieure à " + prg.getBfem_IfEPI(),
                        HttpStatus.NOT_ACCEPTABLE
                );
            }

            if ("I".equals(typeEtab) && cdt.getYear_bfem() > prg.getBfem_IfI()) {
                throw new BusinessResourceException(
                        "diff-year-bfem-error2",
                        "Pour ce candidat, l'année d'obtention du BFEM ne doit pas être supérieure à " + prg.getBfem_IfI(),
                        HttpStatus.NOT_ACCEPTABLE
                );
            }

            // Insertion finale du candidat
            candidatRepository.insert(cdt);

            return null;
        }

        return bm;
    }

    public ConcoursGeneral createConcoursGeneral(ConcoursGeneralDTO concoursGeneralDTO) {
        try {
            // Récupération de l'établissement
            Etablissement etab = etablissementRepository.findByCode(concoursGeneralDTO.getEtablissement());
            if (etab == null) {
                throw new IllegalArgumentException("Établissement non trouvé pour le code : " + concoursGeneralDTO.getEtablissement());
            }

            // Conversion de la date de naissance
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate dateNaissance = LocalDate.parse(concoursGeneralDTO.getDate_birth(), formatter);

            // Construction de l'entité ConcoursGeneral
            ConcoursGeneral cgs = ConcoursGeneral.builder()
                    .firstname(concoursGeneralDTO.getFirstname())
                    .lastname(concoursGeneralDTO.getLastname())
                    .date_birth(dateNaissance)
                    .place_birth(concoursGeneralDTO.getPlace_birth())
                    .phone(concoursGeneralDTO.getPhone())
                    .gender(concoursGeneralDTO.getGender())
                    .classe_0(concoursGeneralDTO.getClasse_0())
                    .note_student_disc(concoursGeneralDTO.getNote_student_disc())
                    .classe_1(concoursGeneralDTO.getClasse_1())
                    .note_classe_disc(concoursGeneralDTO.getNote_classe_disc())
                    .firstname_prof(concoursGeneralDTO.getFirstname_prof())
                    .lastname_prof(concoursGeneralDTO.getLastname_prof())
                    .serie(concoursGeneralDTO.getSerie())
                    .session(concoursGeneralDTO.getSession())
                    .etablissement(etab)
                    .level(concoursGeneralDTO.getLevel())
                    .specialite(concoursGeneralDTO.getSpecialite())
                    .build();

            // Sauvegarde dans la base
            return concoursGeneralRepository.save(cgs);

        } catch (Exception ex) {
            log.error("Erreur lors de la création du candidat : {}", ex.getMessage(), ex);
            throw new RuntimeException("Impossible de créer le candidat.", ex);
        }
    }

    public ConcoursGeneral updateConcoursGeneral(String idCgs, ConcoursGeneralDTO concoursGeneralDTO)
    {
        ConcoursGeneral cgs = concoursGeneralRepository.findById(idCgs).orElse(null);
        if (cgs != null)
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate dateNaissance = LocalDate.parse(concoursGeneralDTO.getDate_birth(), formatter);

            cgs.setFirstname(concoursGeneralDTO.getFirstname());
            cgs.setLastname(concoursGeneralDTO.getLastname());
            cgs.setDate_birth(dateNaissance);
            cgs.setPlace_birth(concoursGeneralDTO.getPlace_birth());
            cgs.setPhone(concoursGeneralDTO.getPhone());
            cgs.setGender(concoursGeneralDTO.getGender());
            cgs.setClasse_0(concoursGeneralDTO.getClasse_0());
            cgs.setNote_student_disc(concoursGeneralDTO.getNote_student_disc());
            cgs.setClasse_1(concoursGeneralDTO.getClasse_1());
            cgs.setNote_classe_disc(concoursGeneralDTO.getNote_classe_disc());
            cgs.setFirstname_prof(concoursGeneralDTO.getFirstname_prof());
            cgs.setLastname_prof(concoursGeneralDTO.getLastname_prof());
            cgs.setSpecialite(concoursGeneralDTO.getSpecialite());
            return concoursGeneralRepository.save(cgs);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Objet with ID " + idCgs + " is not found");
        }
    }

    public ConcoursGeneral updateConcoursGeneral_(String idCgs, ConcoursGeneralDTO concoursGeneralDTO)
    {
        ConcoursGeneral cgs = concoursGeneralRepository.findById(idCgs).orElse(null);
        if (cgs != null)
        {
            List<Rejet> rj = new ArrayList<>(); // ← Initialisation ici

            for (String r : concoursGeneralDTO.getRejets()) {
                Rejet rejet = rejetRepository.findByName(r);
                if (rejet != null)
                {
                    rj.add(rejet);
                }
            }
            if (concoursGeneralDTO.getDecision() == 1 || concoursGeneralDTO.getDecision() == 3)
            {
                rj.clear();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate dateNaissance = LocalDate.parse(concoursGeneralDTO.getDate_birth(), formatter);

            cgs.setFirstname(concoursGeneralDTO.getFirstname());
            cgs.setLastname(concoursGeneralDTO.getLastname());
            cgs.setDate_birth(dateNaissance);
            cgs.setPlace_birth(concoursGeneralDTO.getPlace_birth());
            cgs.setPhone(concoursGeneralDTO.getPhone());
            cgs.setGender(concoursGeneralDTO.getGender());
            cgs.setClasse_0(concoursGeneralDTO.getClasse_0());
            cgs.setNote_student_disc(concoursGeneralDTO.getNote_student_disc());
            cgs.setClasse_1(concoursGeneralDTO.getClasse_1());
            cgs.setNote_classe_disc(concoursGeneralDTO.getNote_classe_disc());
            cgs.setFirstname_prof(concoursGeneralDTO.getFirstname_prof());
            cgs.setLastname_prof(concoursGeneralDTO.getLastname_prof());
            cgs.setSpecialite(concoursGeneralDTO.getSpecialite());
            cgs.setDecision(concoursGeneralDTO.getDecision());
            cgs.setRejets(rj);
            cgs.setOperator(concoursGeneralDTO.getOperator());
            cgs.setDateOperation(LocalDateTime.now());
            return concoursGeneralRepository.save(cgs);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Objet with ID " + idCgs + " is not found");
        }
    }

    public void deleteCGS(String idCdt)
    {
        ConcoursGeneral delete_cgs = concoursGeneralRepository.findById(idCdt).orElse(null);

        if (delete_cgs != null)
        {
            concoursGeneralRepository.delete(delete_cgs);

            auditService.logOperation(

                    "Candidat Supprimé",
                    idCdt + delete_cgs.getFirstname() + delete_cgs.getLastname() + delete_cgs.getEtablissement().getCode(),
                    "DELETE",
                    null, // old values/new values regroupés
                    "CGS-OB",
                    null
            );
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("User with ID " + idCdt + " is not found");
        }
    }

    public Map<String, List<ConcoursGeneral>> getCdtCgsGroupedByClasse(String etablissementId, Long session)
    {
        List<ConcoursGeneral> allCandidates = concoursGeneralRepository.findByEtablissementIdAndSession(etablissementId, session);
        return allCandidates.stream()
                .filter(s -> s.getLevel() != null)
                .collect(Collectors.groupingBy(s -> s.getLevel()));
    }

    public List<Etablissement> getEtablissementsFromCandidats(Long session) {

        List<ConcoursGeneral> candidats = concoursGeneralRepository.findBySession(session);

        return candidats.stream()
                .map(ConcoursGeneral::getEtablissement)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Etablissement::getId, e -> e, (e1, e2) -> e1),
                        m -> new ArrayList<>(m.values())
                ));
    }

    public long countCandidats(String specialite, String classe, Integer session, String etablissementCode) {
        Query query = new Query();

        if (specialite != null && !specialite.isEmpty()) {
            query.addCriteria(Criteria.where("specialite").is(specialite));
        }
        if (classe != null && !classe.isEmpty()) {
            query.addCriteria(Criteria.where("level").is(classe));
        }
        if (session != null) {
            query.addCriteria(Criteria.where("session").is(session));
        }
        if (etablissementCode != null && !etablissementCode.isEmpty()) {
            query.addCriteria(Criteria.where("etablissement.code").is(etablissementCode));
        }

        return mongoTemplate.count(query, "concours_general");
        // "concoursGeneral" = nom de ta collection
    }

    /**
     @Transactional
     public void assignSpecialiteToCgs(SpecialiteAndCgsDTO specialiteAndCgsDTO)
     {
     try
     {
     String specialite = specialiteAndCgsDTO.getSpecialite().getSpecialite();
     String level = specialiteAndCgsDTO.getSpecialite().getClasse();

     List<ConcoursGeneral> candidatsAvecCeSujet =
     Optional.ofNullable(
     concoursGeneralRepository.findByLevelAndSpecialite(level, specialite)
     ).orElse(Collections.emptyList());

     Set<String> idsCandidatsCibles = new HashSet<>(specialiteAndCgsDTO.getCandidats());

     System.out.println(idsCandidatsCibles);

     List<ConcoursGeneral> candidatsAMettreAJour = new ArrayList<>();

     if (!candidatsAvecCeSujet.isEmpty())
     {
     for (ConcoursGeneral c : candidatsAvecCeSujet) {
     if (!idsCandidatsCibles.contains(c.getId())) {
     c.setSpecialite("");
     candidatsAMettreAJour.add(c);
     }
     }
     }


     for (String idCdt : idsCandidatsCibles) {
     ConcoursGeneral cgs = concoursGeneralRepository.findById(idCdt).orElse(null);
     if (cgs != null && !specialite.equals(cgs.getSpecialite())) {
     cgs.setSpecialite(specialite);
     candidatsAMettreAJour.add(cgs);
     }
     }

     if (!candidatsAMettreAJour.isEmpty()) {
     concoursGeneralRepository.saveAll(candidatsAMettreAJour);
     }
     }
     catch (Exception ex)
     {
     log.error(ex.getMessage());
     }
     }
     */


    public List<ConcoursGeneral> getCdtCgsBySpecialite(String level, String specialite)
    {
        return concoursGeneralRepository.findByLevelAndSpecialite(level, specialite);
    }


    public EtatDeVersement createEV(EtatDeVersementDTO etatDeVersementDTO)
    {
        Etablissement etab = etablissementRepository.findById(etatDeVersementDTO.getEtablissement()).orElse(null);
        EtatDeVersement ev = EtatDeVersement.builder()
                .session(etatDeVersementDTO.getSession())
                .file_id(etatDeVersementDTO.getFile_id())
                .count_5000(etatDeVersementDTO.getCount_5000())
                .count_1000_EF(etatDeVersementDTO.getCount_1000_EF())
                .etablissement(etab)
                .date_deposit(LocalDateTime.now())
                .build();

        return etatDeVersementRepository.save(ev);
    }

    public EtatDeVersement updateEV(String idEV, VignetteAddDTO vignetteAddDTO, String firstname, String lastname) {
        EtatDeVersement update_ev = etatDeVersementRepository.findById(idEV).orElse(null);

        if (update_ev == null) {
            throw new NotFoundException("Objet with ID " + idEV + " is not found");
        }

        // Mise à jour uniquement si changement
        update_ev.setCount_1000_EF(vignetteAddDTO.getV1000());
        update_ev.setCount_5000(vignetteAddDTO.getV5000());
        update_ev.setOperator(firstname + " " + lastname);
        update_ev.setDate_ops(LocalDateTime.now());
        update_ev.setState(true);

        EtatDeVersement ev = etatDeVersementRepository.save(update_ev);

        // --- Mise à jour du compte associé ---
        CompteDroitsInscription cmpt_droit_insc = compteDroitInscriptionRepository
                .findByEtablissementIdAndSession(ev.getEtablissement().getId(), ev.getSession());

        if (cmpt_droit_insc == null)
        {
            CompteDroitsInscription compteEtab = new CompteDroitsInscription();
            compteEtab.setSession(ev.getSession());
            compteEtab.setEtablissement(ev.getEtablissement());
            compteEtab.setCount_1000_EF(ev.getCount_1000_EF());
            compteEtab.setCount_5000(ev.getCount_5000());
            compteDroitInscriptionRepository.save(compteEtab);
        }
        else
        {
            cmpt_droit_insc.setCount_1000_EF(cmpt_droit_insc.getCount_1000_EF() + ev.getCount_1000_EF());
            cmpt_droit_insc.setCount_5000(cmpt_droit_insc.getCount_5000() + ev.getCount_5000());
            compteDroitInscriptionRepository.save(cmpt_droit_insc);
        }

        return ev;
    }


    public EtatDeVersement rejectFile(String idEV, boolean rejet) {
        EtatDeVersement update_ev = etatDeVersementRepository.findById(idEV).orElse(null);

        if (update_ev == null) {
            throw new NotFoundException("Objet with ID " + idEV + " is not found");
        }

        update_ev.setInvalid_file(rejet);

        return etatDeVersementRepository.save(update_ev);
    }


    public EtatDeVersement updateEV_(String idEV, String motif, String f, String l) {
        EtatDeVersement update_ev = etatDeVersementRepository.findById(idEV).orElse(null);

        assert update_ev != null;
        Integer v5000 = update_ev.getCount_5000();
        Integer v1000 = update_ev.getCount_1000_EF();

        log.info(String.valueOf(v5000));
        log.info(String.valueOf(v1000));


        if (update_ev == null) {
            throw new NotFoundException("Objet with ID " + idEV + " is not found");
        }

        // Mise à jour uniquement si changement
        update_ev.setCount_1000_EF(0);
        update_ev.setCount_5000(0);
        update_ev.setCorrecteur(f + " " + l);
        update_ev.setMotif_correction_vignettes(motif);
        update_ev.setDate_correction(LocalDateTime.now());
        update_ev.setState(false);

        EtatDeVersement ev = etatDeVersementRepository.save(update_ev);

        // --- Mise à jour du compte associé ---
        CompteDroitsInscription cmpt_droit_insc = compteDroitInscriptionRepository
                .findByEtablissementIdAndSession(ev.getEtablissement().getId(), ev.getSession());

        cmpt_droit_insc.setCount_1000_EF(cmpt_droit_insc.getCount_1000_EF() - v1000);
        cmpt_droit_insc.setCount_5000(cmpt_droit_insc.getCount_5000() - v5000);
        cmpt_droit_insc.setEnabled(false);
        cmpt_droit_insc.setRepresentative("");
        compteDroitInscriptionRepository.save(cmpt_droit_insc);
        return ev;
    }


    public CompteDroitsInscription enabledReception(String idCmptDroitInsc, AutorisationReception autorisationReception)
    {
        CompteDroitsInscription update_cdi = compteDroitInscriptionRepository.findById(idCmptDroitInsc).orElse(null);
        if (update_cdi != null)
        {
            update_cdi.setRepresentative(autorisationReception.getRepresentative());
            update_cdi.setPhone(autorisationReception.getPhone());
            update_cdi.setEnabled(autorisationReception.isEnabled());
            update_cdi.setDateDepot(LocalDateTime.now());
            return compteDroitInscriptionRepository.save(update_cdi);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Objet with ID " + idCmptDroitInsc + " is not found");
        }
    }

    @Transactional
    public Candidat updateCandidat(String idCdt, CandidatDTO candidatDTO, String login, String ip) {
        log.info(candidatDTO.getAdresse());
        Candidat update_cdt = candidatRepository.findById(idCdt).orElse(null);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dateNaissance = LocalDate.parse(candidatDTO.getDate_birth(), formatter);

        Programmation prg = programmationRepository.findByEdition(candidatDTO.getSession());

        if (update_cdt != null) {
            // Génération des champs calculés
            String code = "NO" + candidatDTO.getCentreEtatCivil().getCode()
                    + candidatDTO.getYear_registry_num()
                    + candidatDTO.getRegistry_num()
                    + candidatDTO.getSession();

            String dosNumberBySessionAndEtab = candidatDTO.getDosNumber() + "_"
                    + candidatDTO.getSession() + "_"
                    + (candidatDTO.getEtablissement() != null ? candidatDTO.getEtablissement().getCode() : "NULL");

            // Vérification des doublons en excluant le candidat en cours
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("_id").ne(idCdt), // exclure le candidat en cours
                    new Criteria().orOperator(
                            Criteria.where("numEnrolement").is(code),
                            Criteria.where("dosNumber_by_session_and_etablissement").is(dosNumberBySessionAndEtab),
                            Criteria.where("phone1").is(candidatDTO.getPhone1()),
                            Criteria.where("email").is(candidatDTO.getEmail())
                    )
            ));

            List<Candidat> existing = mongoTemplate.find(query, Candidat.class);

            if (!existing.isEmpty()) {

                for (Candidat c : existing) {
                    if (c.getNumEnrolement().equals(code)) {
                        throw new BusinessResourceException(
                                "numEnrolement-error",
                                "Vos références d'Etat Civil ont été déjà utilisées",
                                HttpStatus.CONFLICT
                        );
                    }
                    if (c.getDosNumber_by_session_and_etablissement().equals(dosNumberBySessionAndEtab)) {
                        throw new BusinessResourceException(
                                "dosNumber-error",
                                "Le numéro de dossier est déjà utilisé pour cette session et cet établissement",
                                HttpStatus.CONFLICT
                        );
                    }
                    if (c.getPhone1().equals(candidatDTO.getPhone1())) {
                        throw new BusinessResourceException(
                                "phone1-error",
                                "Le numéro de téléphone est déjà utilisé",
                                HttpStatus.CONFLICT
                        );
                    }
                    if (c.getEmail().equals(candidatDTO.getEmail())) {
                        throw new BusinessResourceException(
                                "email-error",
                                "L'email est déjà utilisé",
                                HttpStatus.CONFLICT
                        );
                    }


                }
            }

            if (candidatDTO.getYear_bfem() > prg.getBfem_IfEPI() && Objects.equals(candidatDTO.getEtablissement().getTypeEtablissement().getCode(), "EFI"))
            {
                throw new BusinessResourceException(
                        "diff-year-bfem-error1",
                        "Pour ce candidat, l'année d'obtention du BFEM ne doit pas être supérieure à " + prg.getBfem_IfEPI(),
                        HttpStatus.NOT_ACCEPTABLE
                );
            }

            if (candidatDTO.getYear_bfem() > prg.getBfem_IfI() && Objects.equals(candidatDTO.getEtablissement().getTypeEtablissement().getCode(), "I"))
            {
                throw new BusinessResourceException(
                        "diff-year-bfem-error2",
                        "Pour ce candidat, l'année d'obtention du BFEM ne doit pas être supérieure à " + prg.getBfem_IfI(),
                        HttpStatus.NOT_ACCEPTABLE
                );
            }

            // Mise à jour des champs
            update_cdt.setDosNumber(candidatDTO.getDosNumber());
            update_cdt.setFirstname(candidatDTO.getFirstname());
            update_cdt.setLastname(candidatDTO.getLastname());
            // update_cdt.setSession(candidatDTO.getSession()); // tu avais commenté
            update_cdt.setDate_birth(dateNaissance);
            update_cdt.setPlace_birth(candidatDTO.getPlace_birth());
            update_cdt.setGender(candidatDTO.getGender());
            update_cdt.setAdresse(candidatDTO.getAdresse());
            update_cdt.setPhone1(candidatDTO.getPhone1());
            update_cdt.setPhone2(candidatDTO.getPhone2());
            update_cdt.setEmail(candidatDTO.getEmail());
            update_cdt.setCentreEtatCivil(candidatDTO.getCentreEtatCivil());
            update_cdt.setYear_registry_num(candidatDTO.getYear_registry_num());
            update_cdt.setRegistry_num(candidatDTO.getRegistry_num());
            update_cdt.setBac_do_count(candidatDTO.getBac_do_count());
            update_cdt.setYear_bfem(candidatDTO.getYear_bfem());
            update_cdt.setSubject(candidatDTO.getSubject());
            update_cdt.setHandicap(candidatDTO.isHandicap());
            update_cdt.setType_handicap(candidatDTO.getType_handicap());
            update_cdt.setEps(candidatDTO.getEps());
            update_cdt.setCdt_is_cgs(candidatDTO.isCdt_is_cgs());
            update_cdt.setTypeCandidat(candidatDTO.getTypeCandidat());
            update_cdt.setEtablissement(candidatDTO.getEtablissement());
            update_cdt.setSerie(candidatDTO.getSerie());
            update_cdt.setNationality(candidatDTO.getNationality());
            update_cdt.setCountryBirth(candidatDTO.getCountryBirth());
            update_cdt.setMatiere1(candidatDTO.getMatiere1());
            update_cdt.setMatiere2(candidatDTO.getMatiere2());
            update_cdt.setMatiere3(candidatDTO.getMatiere3());
            update_cdt.setEprFacListA(candidatDTO.getEprFacListA());
            update_cdt.setEprFacListB(candidatDTO.getEprFacListB());
            update_cdt.setOrigine_bfem(candidatDTO.getOrigine_bfem());
            update_cdt.setNumEnrolement(code);
            update_cdt.setDosNumber_by_session_and_etablissement(dosNumberBySessionAndEtab);
            update_cdt.setCentreExamen(candidatDTO.getCentreExamen());

            return candidatRepository.save(update_cdt);
        } else {
            throw new NotFoundException("User with ID " + idCdt + " is not found");
        }
    }

    public void deleteCandidat(String idCdt, String login)
    {
        Candidat delete_cdt = candidatRepository.findById(idCdt).orElse(null);

        if (delete_cdt != null)
        {
            try
            {
                candidatRepository.delete(delete_cdt);
                auditService.logOperation(
                        "Candidat Supprimé",
                        idCdt + delete_cdt.getFirstname() + delete_cdt.getLastname(),
                        "DELETE",
                        null, // old values/new values regroupés
                        login,
                        null
                );
            }
            catch (Exception e)
            {
                System.out.println(e);
            }

        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("User with ID " + idCdt + " is not found");
        }
    }

    @Transactional
    public void deleteIsolateCandidat(String idCdt, String login)
    {
        CandidateIsolated candidat = isolatedCandidatRepository.findById(idCdt)
                .orElseThrow(() ->
                        new NotFoundException("User with ID " + idCdt + " is not found")
                );

        isolatedCandidatRepository.delete(candidat);

        auditService.logOperation(
                "Candidat Supprimé",
                idCdt + " " + candidat.getFirstname() + " " + candidat.getLastname(),
                "DELETE",
                null,
                login,
                null
        );
    }

    @Transactional
    public void isolatedCandidat(String idCdt, String login) {

        Candidat candidat = candidatRepository.findById(idCdt)
                .orElseThrow(() ->
                        new NotFoundException("User with ID " + idCdt + " is not found")
                );

        // Création de l'entité isolée (NOUVEL _id)
        CandidateIsolated isolated = new CandidateIsolated(candidat);
        isolated.setId(null);

        isolatedCandidatRepository.save(isolated);

        // Suppression explicite par ID
        candidatRepository.deleteById(idCdt);

        auditService.logOperation(
                "Candidat Isolé",
                idCdt,
                "MOVE Candidat → Isolated",
                null,
                login,
                null
        );
    }

    @Transactional
    public void reintegratedCandidat(String idCdt, String login) {

        CandidateIsolated isolated = isolatedCandidatRepository.findById(idCdt)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Candidat candidat = new Candidat(isolated);
        candidat.setId(null); // IMPORTANT pour Mongo

        candidatRepository.save(candidat);

        isolatedCandidatRepository.deleteById(idCdt);

        auditService.logOperation(
                "Candidat Réintégré",
                idCdt,
                "MOVE Isolated → Candidat",
                null,
                login,
                null
        );
    }



    public List<Candidat> getCandidats()
    {
        return candidatRepository.findAll();
    }

    public List<Candidat> getCandidatsParEtablissement(String etablissementId, Long session)
    {
        List<Candidat> allCandidates = candidatRepository.findByEtablissementIdAndSession(etablissementId, session);
        return allCandidates.stream()
                .filter(s -> s.getSerie() != null && s.getSerie().getCode() != null)
                .filter(s -> s.getDecision() == 0 || s.getDecision() == 1) // A enlever pour afficher les 2 (rejetés)
                .collect(Collectors.toList());
    }

    public Map<String, List<Candidat>> getCandidatsGroupedBySerie(String etablissementId, Long session)
    {
        List<Candidat> allCandidates = candidatRepository.findByEtablissementIdAndSession(etablissementId, session);
        return allCandidates.stream()
                .filter(s -> s.getSerie() != null && s.getSerie().getCode() != null)
                .filter(s -> s.getDecision() == 0 || s.getDecision() == 1) // A enlever pour afficher les 2 (rejetés)
                .collect(Collectors.groupingBy(s -> s.getSerie().getCode()));
    }

    public Map<String, List<Candidat>> getCandidatsGroupedBySujet(String etablissementId, Long session)
    {
        List<Candidat> allCandidates = candidatRepository.findByEtablissementIdAndSession(etablissementId, session);
        return allCandidates.stream()
                .filter(s -> s.getSubject() != null)
                .collect(Collectors.groupingBy(s -> s.getSubject()));
    }

    public List<Sujet> getSujets()
    {
        return sujetRepository.findAll();
    }

    public Sujet createSujet(SujetDTO sujetDTO) {
        Etablissement etab = etablissementRepository.findById(sujetDTO.getEtab_id())
                .orElseThrow(() -> new RuntimeException("Etablissement introuvable"));
        Specialite spec = specialiteRepository.findById(sujetDTO.getSpec_id())
                .orElseThrow(() -> new RuntimeException("Spécialité introuvable"));

        // Chercher le dernier sujet de la même session et du même établissement
        Optional<Sujet> lastSujetOpt = sujetRepository
                .findTopBySessionAndEtablissementOrderByNumSujetDesc(
                        sujetDTO.getSession(), etab);

        int nextNumSujet = 1; // valeur par défaut si nouvelle session ou nouvel etab
        if (lastSujetOpt.isPresent()) {
            nextNumSujet = lastSujetOpt.get().getNumSujet() + 1;
        }

        Sujet sj = Sujet.builder()
                .wording(sujetDTO.getWording())
                .numSujet(nextNumSujet)
                .etablissement(etab)
                .specialite(spec)
                .session(sujetDTO.getSession())
                .build();

        return sujetRepository.save(sj);
    }


    public String updateSujet(String idS, SujetDTO sujetDTO)
    {
        Sujet update_sujet = sujetRepository.findById(idS).orElse(null);
        assert update_sujet != null;
        boolean ok = candidatRepository.existsBySubject(update_sujet.getWording());

        if (ok)
        {
            return "Impossible";
        }
        else
        {
            Specialite spec = specialiteRepository.findById(sujetDTO.getSpec_id()).orElse(null);
            update_sujet.setWording(sujetDTO.getWording());
            update_sujet.setSpecialite(spec);
            sujetRepository.save(update_sujet);
            return "Ok";
        }

    }

    public String deleteSujet(String idS) {
        Sujet sujet = sujetRepository.findById(idS).orElse(null);

        if (sujet == null)
        {
            return "NotFound";
        }

        String sujetWording = sujet.getWording();
        List<Candidat> candidats = candidatRepository.findBySubject(sujetWording);

        if (!candidats.isEmpty())
        {
            for (Candidat c : candidats)
            {
                c.setSubject(null);
            }
            candidatRepository.saveAll(candidats);
        }
        sujetRepository.delete(sujet);
        return "OK";
    }

    public List<Sujet> getSujetsParEtablissement(String etablissementId, Long session) {
        return sujetRepository.findByEtablissementIdAndSession(etablissementId, session);
    }

    public List<SpecialiteCGS> getAllSpecialite() {
        return specialiteCGSRepository.findAll();
    }

    public List<ConcoursGeneral> getCdtsCgsParEtablissement(String etablissementId, Long session) {
        return concoursGeneralRepository.findByEtablissementIdAndSession(etablissementId, session);
    }

    public Candidat getCandidat(int extrait)
    {
        System.out.println("NUM REGISTRE"+extrait);
        Candidat cdt = candidatRepository.findByRegistryNum(extrait);
        return cdt;
    }

    public List<Candidat> getCandidatsBySubject(String etablissementId, Long session, String subject)
    {
        return candidatRepository.findByEtablissementIdAndSessionAndSubject(etablissementId, session, subject);
    }


    public CentreEtatCivil getCECByName(String nameCEC)
    {
        System.out.println("Centre Etat Civil"+nameCEC);
        CentreEtatCivil cec_ = centreEtatCivilRepository.findByName(nameCEC);
        return cec_;
    }

    @Transactional
    public void assignSubjectToCandidate(SujetAndCandidatDTO sujetAndCandidatDTO) {
        String subject = sujetAndCandidatDTO.getSubject().getWording();
        String etabId = sujetAndCandidatDTO.getEtablissementId();
        Long session = sujetAndCandidatDTO.getSession();

        List<Candidat> candidatsAvecCeSujet = candidatRepository.findByEtablissementIdAndSessionAndSubject(etabId, session, subject);

        Set<String> idsCandidatsCibles = new HashSet<>(sujetAndCandidatDTO.getCandidats());

        List<Candidat> candidatsAMettreAJour = new ArrayList<>();

        for (Candidat c : candidatsAvecCeSujet)
        {
            if (!idsCandidatsCibles.contains(c.getId())) {
                c.setSubject("");
                candidatsAMettreAJour.add(c);
            }
        }

        for (String idCdt : idsCandidatsCibles) {
            Candidat cdt = candidatRepository.findById(idCdt).orElse(null);
            if (cdt != null && !subject.equals(cdt.getSubject())) {
                cdt.setSubject(subject);
                candidatsAMettreAJour.add(cdt);
            }
        }

        if (!candidatsAMettreAJour.isEmpty()) {
            candidatRepository.saveAll(candidatsAMettreAJour);
        }
    }


    /**
     public List<Candidat> getFilteredCandidats(String etablissementId, String serieCode, Long session) {
     return candidatRepository.findByEtablissementIdAndSerieCodeAndSession(etablissementId, serieCode, session);
     }**/

    public List<Candidat> getFilteredCandidats(String etablissementId, Long session) {
        return candidatRepository.findByEtablissementIdAndSession(etablissementId, session);
    }

    public List<CandidateIsolated> getIsolatedCandidats(String etablissementId, Long session) {
        return isolatedCandidatRepository.findByEtablissementIdAndSession(etablissementId, session);
    }

    private long toLong(String s) {
        try {
            return Long.parseLong(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    public List<Candidat> getFilteredCandidatsForPdf(String etablissementId, Long session, String sortBy, String serieCode)
    {
        List<Candidat> candidats =  candidatRepository.findByEtablissementIdAndSessionAndSerieCode(etablissementId, session, serieCode);

        // Trier en Java après récupération (option simple)
        if ("lastname".equalsIgnoreCase(sortBy))
        {
            candidats.sort(Comparator.comparing(Candidat::getLastname, String.CASE_INSENSITIVE_ORDER));
        }
        else if ("dosNumber".equalsIgnoreCase(sortBy))
        {
            candidats.sort(Comparator.comparingLong(c -> toLong(c.getDosNumber())));
        }

        return candidats;

    }


    public List<Candidat> getFilteredOneCandidatsForPdf(String etablissementId, Long session, String serieCode, String start)
    {
        List<Candidat> candidats =  candidatRepository.findByEtablissementIdAndSessionAndSerieCodeAndDosNumber(etablissementId, session, serieCode, start);
        return candidats;
    }


    public List<Candidat> getFilteredCandidatsByBoundsPdf(String etablissementId, Long session, String sortBy, String serieCode, Long start, Long end)
    {
        List<Candidat> candidats =  new ArrayList<>(candidatRepository.findByDosNumberIntervalAgg(etablissementId, session, serieCode, start, end));

        // Trier en Java après récupération (option simple)
        if ("lastname".equalsIgnoreCase(sortBy))
        {
            candidats.sort(Comparator.comparing(Candidat::getLastname, String.CASE_INSENSITIVE_ORDER));
        }
        else if ("dosNumber".equalsIgnoreCase(sortBy))
        {
            candidats.sort(Comparator.comparingLong(c -> toLong(c.getDosNumber())));
        }
        return candidats;
    }

    public List<Candidat> getAllCandidatsForPdf(String etablissementId, Long session, String sortBy)
    {
        List<Candidat> candidats =  candidatRepository.findByEtablissementIdAndSession(etablissementId, session);

        if ("lastname".equalsIgnoreCase(sortBy))
        {
            candidats.sort(Comparator.comparing(Candidat::getLastname, String.CASE_INSENSITIVE_ORDER));
        }
        else if ("dosNumber".equalsIgnoreCase(sortBy))
        {
            candidats.sort(Comparator.comparingLong(c -> toLong(c.getDosNumber())));
        }

        return candidats;

    }


    public List<Candidat> getFilteredCandidatsForPdfOL(String etablissementId, Long session, String serieCode)
    {
        List<Candidat> candidats =  candidatRepository.findByEtablissementIdAndSessionAndSerieCodeAndDecision(etablissementId, session, serieCode, 1);
        candidats.sort(Comparator.comparing(Candidat::getLastname, String.CASE_INSENSITIVE_ORDER));
        return candidats;

    }

    public List<Candidat> getAllCandidatsForPdfOL(String etablissementId, Long session)
    {
        List<Candidat> candidats =  candidatRepository.findByEtablissementIdAndSessionAndDecision(etablissementId, session, 1);
        candidats.sort(Comparator.comparing(Candidat::getLastname, String.CASE_INSENSITIVE_ORDER));
        return candidats;

    }

    public List<Candidat> getFilteredCandidatsForPdfSujet(String etablissementId, Long session, String subject)
    {
        List<Candidat> candidats =  candidatRepository.findByEtablissementIdAndSessionAndSubject(etablissementId, session, subject);
        candidats.sort(Comparator.comparing(Candidat::getLastname, String.CASE_INSENSITIVE_ORDER));
        return candidats;

    }

    public List<Candidat> getAllCandidatsForPdfSujet(String etablissementId, Long session)
    {
        List<Candidat> candidats =
                candidatRepository.findByEtablissementIdAndSession(etablissementId, session);

        // Filtrer + trier
        return candidats.stream()
                .filter(c -> c.getSubject() != null && !c.getSubject().trim().isEmpty())
                .sorted(
                        Comparator.comparing(Candidat::getSubject, String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(Candidat::getLastname, String.CASE_INSENSITIVE_ORDER)
                )
                .toList();
    }

    public List<ConcoursGeneral> getFilteredCandidatsForPdfCGS(String etablissementId, Long session, String specialite, String level)
    {
        List<ConcoursGeneral> candidats =  concoursGeneralRepository.findByEtablissementIdAndSessionAndSpecialiteAndLevel(etablissementId, session, specialite, level);
        return candidats;

    }

    public List<Candidat> getFilteredCandidatsForPdfReject(String etablissementId, Long session)
    {
        return candidatRepository.findByEtablissementIdAndSession(etablissementId, session);
    }

    public List<StatsDTO> getNombreCandidatsParSerie(String etabId, Long session)
    {
        List<Candidat> candidats = candidatRepository.findBySessionAndEtablissement_Id(session, etabId);

        Map<String, Long> groupedBySerie = candidats.stream()
                .filter(c -> c.getSerie() != null && c.getSerie().getCode() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getSerie().getCode(),
                        Collectors.counting()
                ));

        return groupedBySerie.entrySet().stream()
                .map(entry -> new StatsDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getData(), a.getData())) // Tri décroissant
                .collect(Collectors.toList());
    }

    public List<StatsDTO> getNombreCandidatsParSexe(String etabId, Long session)
    {
        List<Candidat> candidats = candidatRepository.findBySessionAndEtablissement_Id(session, etabId);

        Map<Object, Long> groupedBySexe = candidats.stream()
                .filter(c -> c.getGender() != null)
                .collect(Collectors.groupingBy(
                        Candidat::getGender,
                        Collectors.counting()
                ));


        return groupedBySexe.entrySet().stream()
                .map(entry -> new StatsDTO(entry.getKey().toString(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getData(), a.getData())) // Tri décroissant
                .collect(Collectors.toList());
    }

    public List<StatsDTO> getNombreCandidatsParEPS(String etabId, Long session)
    {
        List<Candidat> candidats = candidatRepository.findBySessionAndEtablissement_Id(session, etabId);

        Map<String, Long> groupedBySerie = candidats.stream()
                .filter(c -> c.getEps() != null)
                .collect(Collectors.groupingBy(
                        Candidat::getEps,
                        Collectors.counting()
                ));

        return groupedBySerie.entrySet().stream()
                .map(entry -> new StatsDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getData(), a.getData())) // Tri décroissant
                .collect(Collectors.toList());
    }

    public List<StatsDTO> getNombreCandidatsParHandicap(String etabId, Long session)
    {
        List<Candidat> candidats = candidatRepository.findBySessionAndEtablissement_Id(session, etabId);

        Map<String, Long> groupedBySerie = candidats.stream()
                .filter(c -> c.getType_handicap() != null)
                .collect(Collectors.groupingBy(
                        Candidat::getType_handicap,
                        Collectors.counting()
                ));

        return groupedBySerie.entrySet().stream()
                .map(entry -> new StatsDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getData(), a.getData())) // Tri décroissant
                .collect(Collectors.toList());
    }

    public List<StatsDTO> getNombreCandidatsParEF(String etabId, Long session)
    {
        List<Candidat> candidats = candidatRepository.findBySessionAndEtablissement_Id(session, etabId);

        Map<String, Long> groupedByEF = new HashMap<>();

        for (Candidat c : candidats) {
                if (!c.getEprFacListA().name().equals("Aucun"))
                {
                    groupedByEF.merge(c.getEprFacListA().toString(), 1L, Long::sum);
                }
                if (c.getEprFacListB() != null) {
                    groupedByEF.merge(c.getEprFacListB().getName(), 1L, Long::sum);
                }
        }

        return groupedByEF.entrySet().stream()
                .map(entry -> new StatsDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getData(), a.getData()))
                .collect(Collectors.toList());
    }

    public List<StatsDTO> getNombreCandidatsParOptions(String etabId, Long session)
    {
        List<Candidat> candidats = candidatRepository.findBySessionAndEtablissement_Id(session, etabId);

        Map<String, Long> groupedByOPT = new HashMap<>();

        for (Candidat c : candidats)
        {
                if (c.getMatiere1() != null) {
                    groupedByOPT.merge(c.getMatiere1().getName(), 1L, Long::sum);
                }
                if (c.getMatiere2() != null) {
                    groupedByOPT.merge(c.getMatiere2().getName(), 1L, Long::sum);
                }
                if (c.getMatiere3() != null) {
                    groupedByOPT.merge(c.getMatiere3().getName(), 1L, Long::sum);
                }
        }

        return groupedByOPT.entrySet().stream()
                .map(entry -> new StatsDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getData(), a.getData()))
                .collect(Collectors.toList());
    }


    public Candidat updateDecision(String idCdt, CandidatDecisionDTO candidatDecisionDTO, String login, String ip)
    {
        try
        {
            Candidat update_cdt = candidatRepository.findById(idCdt).orElse(null);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate dateNaissance = LocalDate.parse(candidatDecisionDTO.getDate_birth(), formatter);

            /***
             TypeCandidat typeCandidat = typeCandidatRepository.findByName(candidatDTO.getType_candidat_name());
             Etablissement etab = etablissementRepository.findByName(candidatDTO.getEtab_name());
             CentreEtatCivil cec = centreEtatCivilRepository.findByName(candidatDTO.getCentre_etat_civil_name());
             Serie serie = serieRepository.findByCode(candidatDTO.getSerie_name());
             Nationality nat = nationalityRepository.findByName(candidatDTO.getNationality_name());
             ***/

            //List<String> opta = candidatDTO.getOptions();

            Map<String, Object> diffs = auditService.getDifferences(update_cdt, candidatDecisionDTO);


            String code = "NO" +
                    candidatDecisionDTO.getCentreEtatCivil().getCode() +
                    candidatDecisionDTO.getYear_registry_num() +
                    candidatDecisionDTO.getRegistry_num() +
                    candidatDecisionDTO.getSession();

            String dosNumberBySessionAndEtab = candidatDecisionDTO.getDosNumber() + "_" +
                    candidatDecisionDTO.getSession() + "_" +
                    (candidatDecisionDTO.getEtablissement() != null
                            ? candidatDecisionDTO.getEtablissement().getCode()
                            : "NULL");

            if (update_cdt != null) {
                List<Rejet> rj = new ArrayList<>(); // ← Initialisation ici

                for (String r : candidatDecisionDTO.getMotif()) {
                    Rejet rejet = rejetRepository.findByName(r);
                    if (rejet != null)
                    {
                        rj.add(rejet);
                    }
                }
                if (candidatDecisionDTO.getDecision() == 1)
                {
                    rj.clear();
                }
                update_cdt.setDosNumber(candidatDecisionDTO.getDosNumber());
                update_cdt.setNumEnrolement(code);
                update_cdt.setDosNumber_by_session_and_etablissement(dosNumberBySessionAndEtab);
                update_cdt.setFirstname(candidatDecisionDTO.getFirstname());
                update_cdt.setLastname(candidatDecisionDTO.getLastname());
                //update_cdt.setSession(candidatDTO.getSession());
                update_cdt.setDate_birth(dateNaissance);
                update_cdt.setPlace_birth(candidatDecisionDTO.getPlace_birth());
                update_cdt.setGender(candidatDecisionDTO.getGender());
                update_cdt.setPhone1(candidatDecisionDTO.getPhone1());
                update_cdt.setPhone2(candidatDecisionDTO.getPhone2());
                update_cdt.setEmail(candidatDecisionDTO.getEmail());
                update_cdt.setCentreEtatCivil(candidatDecisionDTO.getCentreEtatCivil());
                update_cdt.setYear_registry_num(candidatDecisionDTO.getYear_registry_num());
                update_cdt.setRegistry_num(candidatDecisionDTO.getRegistry_num());
                update_cdt.setBac_do_count(candidatDecisionDTO.getBac_do_count());
                update_cdt.setYear_bfem(candidatDecisionDTO.getYear_bfem());
                update_cdt.setSubject(candidatDecisionDTO.getSubject());
                update_cdt.setHandicap(candidatDecisionDTO.isHandicap());
                update_cdt.setType_handicap(candidatDecisionDTO.getType_handicap());
                update_cdt.setEps(candidatDecisionDTO.getEps());
                update_cdt.setCdt_is_cgs(candidatDecisionDTO.isCdt_is_cgs());
                update_cdt.setCentreExamen(candidatDecisionDTO.getCentreExamen());
                //update_cdt.setTypeCandidat(candidatDecisionDTO.getTypeCandidat());
                //update_cdt.setEtablissement(candidatDecisionDTO.getEtablissement());
                update_cdt.setSerie(candidatDecisionDTO.getSerie());
                update_cdt.setNationality(candidatDecisionDTO.getNationality());
                update_cdt.setCountryBirth(candidatDecisionDTO.getCountryBirth());
                update_cdt.setMatiere1(candidatDecisionDTO.getMatiere1());
                update_cdt.setMatiere2(candidatDecisionDTO.getMatiere2());
                update_cdt.setMatiere3(candidatDecisionDTO.getMatiere3());
                update_cdt.setEprFacListA(candidatDecisionDTO.getEprFacListA());
                update_cdt.setEprFacListB(candidatDecisionDTO.getEprFacListB());
                update_cdt.setOrigine_bfem(candidatDecisionDTO.getOrigine_bfem());

                update_cdt.setDecision(candidatDecisionDTO.getDecision());
                update_cdt.setRejets(rj);
                update_cdt.setOperator(candidatDecisionDTO.getOperator());
                update_cdt.setDateOperation(LocalDateTime.now());

                Candidat saved = candidatRepository.save(update_cdt);

                auditService.logOperation(
                        "Candidat Modifié",
                        idCdt,
                        "PUT",
                        Map.of("modified", diffs), // old values/new values regroupés
                        login,
                        ip
                );

                return saved;

            }

            else
            {
                // Handle the case where the user is not found
                throw new NotFoundException("User with ID " + idCdt + " is not found");
            }
        } catch (Exception e) {
            e.printStackTrace(); // ← à lire dans la console
            throw new TechnicalException("Erreur technique : " + e.getMessage());
        }


    }

    public Page<CandidatToCampusen> getCandidatsValidesPourCampusen(Long sessionId, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        return candidatToCampusenRepository.findBySession(sessionId, pageable);
    }

    public List<Rejet> getRejets() {
        return rejetRepository.findAll();
    }

    public List<EtatDeVersement> getFilteredEVs(String etablissementId, Long session)
    {
        return etatDeVersementRepository.findByEtablissementIdAndSession(etablissementId, session, Sort.by(Sort.Direction.DESC, "date_deposit"));
    }

    public List<EtatDeVersement> getFilteredEVs_(Long session)
    {
        return etatDeVersementRepository.findBySession(session, Sort.by(Sort.Direction.DESC, "date_deposit"));
    }

    public CompteDroitsInscription getCompteDroitsInscription(String establishmentId, Long session) {
        return compteDroitInscriptionRepository.findByEtablissementIdAndSession(establishmentId, session);
    }

    public List<CompteDroitsInscription> getMandataires(Long session) {
        return compteDroitInscriptionRepository.findBySession(session);
    }

    // Recupération des comptes FAEB
    public List<CompteDroitsInscription> getFaeb(Long session) {
        return compteDroitInscriptionRepository.findBySessionOrderByEtablissementInspectionAcademieNameAsc(session);
    }

    public long countBySessionAndEtablissementIdWhereEprFacListANotNullAndNotAucun(Long session, String etablissementId) {
        Query query = Query.query(
                new Criteria().andOperator(
                        Criteria.where("session").is(session),
                        Criteria.where("etablissement.id").is(etablissementId),
                        Criteria.where("eprFacListA")
                                .exists(true)
                                .nin("Aucun")
                )
        );

        // soit Candidat.class si tu as la classe mappée, soit le nom de collection "candidat"
        return mongoTemplate.count(query, Candidat.class);
    }

    public Map<String, Long> compterFacultatives(String etablissementId, Long session)
    {
        long countA = countBySessionAndEtablissementIdWhereEprFacListANotNullAndNotAucun(session, etablissementId);
        long countB = candidatRepository.countBySessionAndEtablissement_IdAndEprFacListBNotNull(session, etablissementId);
        long cdt = candidatRepository.countBySessionAndEtablissement_Id(session, etablissementId);

        return Map.of(
                "facListA", countA,
                "facListB", countB,
                "candidats", cdt
        );
    }

    public Candidat checkDoublon(int yearRegistryNum, String registryNum, String cec, Long session)
    {
        return candidatRepository.findCandidate(yearRegistryNum, registryNum, cec, session);
    }

    public Candidat checkDoublonNumTel(String phone1, Long session)
    {
        return candidatRepository.findByPhone1AndSession(phone1, session);
    }

    public Candidat checkDoublonEmail(String email, Long session)
    {
        return candidatRepository.findByEmailAndSession(email, session);
    }

    public Candidat checkByDosNumber(String dosNumber, Long session, String etablissementId)
    {
        return candidatRepository.findByDosNumberAndSessionAndEtablissement_Id(dosNumber, session, etablissementId);
    }

    public List<EtablissementSummaryReceptionScolarite> summarizeScolariteOps(Long session, String receptionniste)
    {
        MatchOperation match = Aggregation.match(
                Criteria.where("session").is(session)
                        .and("etablissement").ne(null)
        );

        GroupOperation group = Aggregation.group("etablissement._id")
                .first("etablissement").as("etablissement")
                // décision0 = tous les candidats en attente, indépendant de l'opérateur
                .sum(
                        ConditionalOperators.when(
                                Criteria.where("decision").is(0)
                        ).then(1).otherwise(0)
                ).as("decision0")

                .sum(ConditionalOperators.when(
                        new Criteria().andOperator(
                                Criteria.where("decision").is(1),
                                Criteria.where("operator").is(receptionniste)
                        )).then(1).otherwise(0)
                ).as("decision1")
                // décision2 = seulement si l'opérateur correspond
                .sum(ConditionalOperators.when(
                        new Criteria().andOperator(
                                Criteria.where("decision").is(2),
                                Criteria.where("operator").is(receptionniste)
                        )).then(1).otherwise(0)
                ).as("decision2");

        LookupOperation lookupCompte = Aggregation.lookup(
                "compte_droits_inscription",
                "etablissement._id",
                "etablissement._id",
                "compte"
        );

        UnwindOperation unwindCompte = Aggregation.unwind("compte", true);

        ProjectionOperation project = Aggregation.project()
                .and("etablissement").as("etablissement")
                .and("decision0").as("decision0")
                .and("decision1").as("decision1")
                .and("decision2").as("decision2")
                .andExpression("'" + receptionniste + "'").as("operator")
                .and("compte.representative").as("representative")
                .and("compte.phone").as("phone")
                .and("compte.count_5000").as("count_5000")
                .and("compte.dateDepot").as("dateDepot");

        MatchOperation matchAfterGroup = Aggregation.match(
                new Criteria().orOperator(
                        Criteria.where("decision1").gt(0),
                        Criteria.where("decision2").gt(0)
                )
        );

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                group,
                lookupCompte,
                matchAfterGroup,
                unwindCompte,
                project
        );

        List<EtablissementSummaryReceptionScolarite> summaries = mongoTemplate
                .aggregate(aggregation, "candidat", EtablissementSummaryReceptionScolarite.class)
                .getMappedResults();

        return summaries.stream()
                .filter(s -> s.getDecision1() > 0 || s.getDecision2() > 0)
                .collect(Collectors.toList());
    }


    public List<Object> summarizeSchoolWithDossierEnAttente(Long session) {

        // 1️⃣ Filtrer les dossiers en attente
        MatchOperation match = Aggregation.match(
                Criteria.where("session").is(session)
                        .and("etablissement").ne(null)
                        .and("decision").is(0)
        );

        // 2️⃣ Grouper par établissement (au moins un dossier en attente)
        GroupOperation group = Aggregation.group("etablissement._id")
                .first("etablissement").as("etablissement")
                .count().as("nbDossiersEnAttente");

        LookupOperation lookupUser = Aggregation.lookup(
                "user",
                "etablissement._id",
                "acteur.etablissement._id",
                "user_"
        );

        UnwindOperation unwindUser = Aggregation.unwind("user_", true);

        ProjectionOperation project = Aggregation.project()
                .and("etablissement").as("etablissement")
                .and("nbDossiersEnAttente").as("nbDossiersEnAttente")
                .and("user_.phone").as("phone");

        // 3️⃣ Agrégation
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                group,
                lookupUser,
                unwindUser,
                project
        );

        // 4️⃣ Exécution
        return mongoTemplate
                .aggregate(
                        aggregation,
                        "candidat",
                        Object.class
                )
                .getMappedResults();
    }


    public List<Object> etablissementReceivedByOps(Integer session, String receptionniste)
    {

        MatchOperation match = Aggregation.match(
                Criteria.where("session").is(session)
                        .and("etablissement").ne(null)
        );

        GroupOperation group = Aggregation.group("etablissement._id")
                .first("etablissement").as("etablissement")
                .sum(
                        ConditionalOperators.when(
                                Criteria.where("decision").is(0)
                        ).then(1).otherwise(0)
                ).as("decision0")
                .sum(ConditionalOperators.when(
                        new Criteria().andOperator(
                                Criteria.where("decision").is(1),
                                Criteria.where("operator").is(receptionniste)
                        )).then(1).otherwise(0)
                ).as("decision1")
                // décision2 = seulement si l'opérateur correspond
                .sum(ConditionalOperators.when(
                        new Criteria().andOperator(
                                Criteria.where("decision").is(2),
                                Criteria.where("operator").is(receptionniste)
                        )).then(1).otherwise(0)
                ).as("decision2");

        LookupOperation lookupCompte = Aggregation.lookup(
                "compte_droits_inscription",
                "etablissement._id",
                "etablissement._id",
                "compte"
        );

        UnwindOperation unwindCompte = Aggregation.unwind("compte", true);

        ProjectionOperation project = Aggregation.project()
                .and("etablissement").as("etablissement")
                .and("decision0").as("decision0")
                .and("decision1").as("decision1")
                .and("decision2").as("decision2")
                .andExpression("'" + receptionniste + "'").as("operator")
                .and("compte.representative").as("representative")
                .and("compte.phone").as("phone")
                .and("compte.count_5000").as("count_5000")
                .and("compte.dateDepot").as("dateDepot");

        MatchOperation matchAfterGroup = Aggregation.match(
                new Criteria().orOperator(
                        Criteria.where("decision1").gt(0),
                        Criteria.where("decision2").gt(0)
                )
        );

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                group,
                matchAfterGroup,
                lookupCompte,
                unwindCompte,
                project
        );

        List<Object> results = mongoTemplate
                .aggregate(aggregation, "candidat", Object.class)
                .getMappedResults();

        //log.info("Résultat agrégation (DEBUG COMPTE) : {}", results);

        return results;
    }

    public List<Document> summarizeOperatorsByEtablissement(Integer session, String etablissementId) {

        // 1️⃣ Filtrer les candidats pour la session et l'établissement
        MatchOperation match = Aggregation.match(
                Criteria.where("session").is(session)
                        .and("etablissement._id").is(new ObjectId(etablissementId))
                        .and("operator").ne(null)
        );

        // 2️⃣ Lookup vers la collection user (operator = login)
        LookupOperation lookupUser = LookupOperation.newLookup()
                .from("user")
                .localField("operator")  // login dans candidat
                .foreignField("login")   // login dans user
                .as("operator");

        // 3️⃣ Unwind pour récupérer le user de l'array généré par lookup
        UnwindOperation unwindUser = Aggregation.unwind("operator");

        // 4️⃣ Grouper par établissement et construire les infos operator
        GroupOperation group = Aggregation.group("etablissement._id")
                .first("_id").as("etablissementId")
                .addToSet(
                        new Document("id", "$operator._id")
                                .append("firstname", "$operator.firstname")
                                .append("lastname", "$operator.lastname")
                                .append("login", "$operator.login")
                                .append("phone", "$operator.phone")
                ).as("operators");

        // 5️⃣ Création de l'agrégation
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                lookupUser,
                unwindUser,
                group
        );

        // 6️⃣ Exécuter l'agrégation
        return mongoTemplate
                .aggregate(aggregation, "candidat", Document.class)
                .getMappedResults();
    }

    public List<EtablissementSummaryReception> summarize(Long session, String ia)
    {
        List<Etablissement> etablissements = new ArrayList<>();
        Map<String, Etablissement> etabById = etablissementRepository
                .findByInspectionAcademieCode(ia)
                .stream()
                .peek(etablissements::add)  // remplit la liste en même temps
                .collect(Collectors.toMap(Etablissement::getId, Function.identity()));

        List<Candidat> candidats = candidatRepository.findBySessionAndEtablissementInspectionAcademieCode(session, ia);

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
                                    List<String> operators = new ArrayList<>();
                                    if (c.getOperator() != null)
                                    {
                                        operators.add(c.getOperator());
                                    }
                                    s.setOperators(operators);
                                    updateSummary(s, c.getDecision());
                                    return s;
                                },
                                (s1, s2) -> { // Merge en cas de même établissement
                                    if (s2.getOperators() != null) {
                                        s1.getOperators().addAll(
                                                s2.getOperators().stream()
                                                        .filter(Objects::nonNull) // filtre les null
                                                        .filter(op -> !s1.getOperators().contains(op)) // éviter doublons
                                                        .toList()
                                        );
                                    }
                                    s2.copyInto(s1);
                                    return s1;
                                }
                        ));

        // 5. Ajouter les établissements sans candidats (approche fonctionnelle)
        etablissements.forEach(etab ->
                summaries.putIfAbsent(etab, emptySummary(etab))
        );

        // Retourner la liste des résumés des établissements
        return new ArrayList<>(summaries.values());
    }


    private void updateSummary(EtablissementSummaryReception s, int decision) {
        switch (decision) {
            case 1 -> s.incrementDecision1();
            case 2 -> s.incrementDecision2();
            default -> s.incrementDecision0();
        }
    }

    private EtablissementSummaryReception emptySummary(Etablissement etab) {
        EtablissementSummaryReception s = new EtablissementSummaryReception();
        s.setEtablissement(etab);
        return s;
    }


    public EtablissementSummaryReception_ summarize_(Long session)
    {
        EtablissementSummaryReception_ summary = new EtablissementSummaryReception_();

        summary.setDecision1(candidatRepository.countBySessionAndDecision(session, 1));
        summary.setDecision2(candidatRepository.countBySessionAndDecision(session, 2));
        summary.setDecision0(candidatRepository.countBySessionAndDecision(session, 0));

        return summary;
    }

    public EtablissementSummaryReception_ summarize_CGS(Long session)
    {
        EtablissementSummaryReception_ summary = new EtablissementSummaryReception_();

        summary.setDecision1(concoursGeneralRepository.countBySessionAndDecision(session, 1));
        summary.setDecision2(concoursGeneralRepository.countBySessionAndDecision(session, 2));
        summary.setDecision0(concoursGeneralRepository.countBySessionAndDecision(session, 0));

        return summary;
    }

    public Page<Candidat> getAllCdtsBySession(int page, int size, long session)
    {
        Pageable pageable = PageRequest.of(page, size);
        return candidatRepository.readBySession(session, pageable);
    }

    public List<CentreExamen> getCentresExamenByEtablissement(String etablissementId, Integer session)
    {
        return candidatRepository.findCentresExamenByEtablissementAndSession(
                new ObjectId(etablissementId),
                session
        );
    }

    public Page<Candidat> getAllCandidatsOfAcademia(Pageable pageable, Integer session, String iaCode) {
        // Requête paginée avec filtres dynamiques
        return candidatRepository.findBySessionAndIaCode(session, iaCode, pageable);
    }


    public List<Serie> getSeriesInAcademia(String iaCode, Integer session) {
        // MongoDB query via repository custom
        List<Candidat> candidats = candidatRepository.findBySessionAndIaCode_(session, iaCode);

        // Extraire les séries uniques
        return candidats.stream()
                .map(Candidat::getSerie)   // récupérer l'objet Serie
                .filter(Objects::nonNull)
                .distinct()                // éliminer les doublons
                .collect(Collectors.toList());
    }


    public List<Candidat> getAllCandidatsBySerieAndSexe(Long session, String iaCode, String serieCode)
    {
        List<Candidat> response;

        if (serieCode != null)
        {
            response = candidatRepository.findBySessionAndIaCodeAndSerieCode(session, iaCode, serieCode);
        }
        else
        {
            response = candidatRepository.findBySessionAndIaCode(session, iaCode);
        }
        return response;
    }

    public List<CandidateIsolated> getAllIsoCandidatsBySerieAndSexe(Long session, String iaCode, String serieCode)
    {
        List<CandidateIsolated> response;

        if (serieCode != null)
        {
            response = isolatedCandidatRepository.findIsoBySessionAndIaCodeAndSerieCode(session, iaCode, serieCode);
        }
        else
        {
            response = isolatedCandidatRepository.findIsoBySessionAndIaCode(session, iaCode);
        }
        return response;
    }



}