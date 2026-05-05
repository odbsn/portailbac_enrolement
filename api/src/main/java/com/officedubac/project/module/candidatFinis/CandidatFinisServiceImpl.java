package com.officedubac.project.module.candidatFinis;

import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.exception.DuplicateResourceException;
import com.officedubac.project.exception.ResourceNotFoundException;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.models.User;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisRequest;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisResponse;
import com.officedubac.project.module.candidatFinis.dto.PageResponse;
import com.officedubac.project.module.convocations.ConvocationPdfService;
import com.officedubac.project.module.convocations.kafka.ConvocationStorageConfig;
import com.officedubac.project.module.epreuve.Epreuve;
import com.officedubac.project.module.epreuve.EpreuveMapper;
import com.officedubac.project.module.epreuve.dto.EpreuveResponse;
import com.officedubac.project.module.jour.Jour;
import com.officedubac.project.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatFinisServiceImpl implements CandidatFinisService {

    private final CandidatFinisRepository candidatFinisRepository;
    private final MongoTemplate mongoTemplate;
    private final CandidatFinisMapper candidatFinisMapper;
    private final EpreuveMapper epreuveMapper;
    private final AuthenticationService authService;
    private final CandidatExportService candidatExportService;
    private final ConvocationPdfService convocationPdfService;
    private final ConvocationStorageConfig storageConfig;
    private final ConvocationReactiveService convocationReactiveService;

    private static final String COLLECTION_NAME = "candidat_finis";
    private static final String EPREUVE_COLLECTION = "epreuve";

    /**
     * Convertit une Page Spring Data en PageResponse personnalisée
     */
    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

//    public ConvocationDTO getCandidatConvocation(String codeEtab, String numeroTable, String dateNaissance) {
//
//        Query query = new Query();
//
//        query.addCriteria(Criteria.where("etablissement.code").is(codeEtab)
//                .and("numeroTable").is(numeroTable)
//                .and("dateNaissance").is(dateNaissance));
//
//        // projection = performance MAX
//        query.fields()
//                .include("prenoms")
//                .include("nom")
//                .include("dateNaissance")
//                .include("lieuNaissance")
//                .include("nationalite")
//                .include("numeroTable")
//                .include("jury")
//                .include("serie")
//                .include("sexe")
//                .include("typeCandidat")
//                .include("eps")
//                .include("mo1").include("mo2").include("mo3")
//                .include("ef1").include("ef2")
//                .include("centreMatFac1").include("libMatFac1")
//                .include("centreMatFac2").include("libMatFac2")
//                .include("centreEcrit.name")
//                .include("centreEcritParticulier")
//                .include("centreActEPS.name")
//                .include("etablissement.name");
//
//        Document doc = mongoTemplate.findOne(query, Document.class, "candidat_finis");
//
//        if (doc == null) {
//            throw new RuntimeException("Candidat non trouvé");
//        }
//
//        return new ConvocationDTO(
//                doc.getString("prenoms"),
//                doc.getString("nom"),
//                doc.getString("dateNaissance"),
//                doc.getString("lieuNaissance"),
//                doc.getString("nationalite"),
//
//                doc.getString("numeroTable"),
//                doc.getString("jury"),
//                doc.getString("serie"),
//                doc.getString("sexe"),
//
//                doc.getString("typeCandidat"),
//                doc.getString("eps"),
//
//                ((Document) doc.get("etablissement")).getString("name"),
//
//                doc.get("centreEcrit") != null ? ((Document) doc.get("centreEcrit")).getString("name") : null,
//                doc.getString("centreEcritParticulier"),
//                doc.get("centreActEPS") != null ? ((Document) doc.get("centreActEPS")).getString("name") : null,
//
//                doc.getString("mo1"),
//                doc.getString("mo2"),
//                doc.getString("mo3"),
//                doc.getString("ef1"),
//                doc.getString("ef2"),
//
//                doc.getString("centreMatFac1"),
//                doc.getString("libMatFac1"),
//                doc.getString("centreMatFac2"),
//                doc.getString("libMatFac2")
//        );
//    }
@Override
public Etablissement getEtablissementUtilisateurConnecte() {
        User appUser = authService.getCurrentUser();

        if (appUser == null) {
            throw new BusinessResourceException("unauthorized",
                    "Utilisateur non connecté", HttpStatus.UNAUTHORIZED);
        }

        log.info("Utilisateur connecté: {}", appUser.getLogin());

        Etablissement etablissement = null;

        // L'acteur est directement un objet intégré dans User, pas une référence DBRef
        if (appUser.getActeur() != null) {
            // Récupérer l'établissement depuis l'objet acteur
            etablissement = appUser.getActeur().getEtablissement();
            if (etablissement != null) {
                log.info("Établissement récupéré: {} ({})",
                        etablissement.getName(), etablissement.getId());
            } else {
                log.warn("L'acteur n'a pas d'établissement associé");
            }
        } else {
            log.warn("L'utilisateur n'a pas d'acteur associé");
        }

        if (etablissement == null) {
            throw new BusinessResourceException("no-establishment",
                    "Aucun établissement associé à l'utilisateur: " + appUser.getLogin(),
                    HttpStatus.BAD_REQUEST);
        }

        return etablissement;
    }

    /**
     * Récupère les épreuves pour une série donnée avec tri par ordre de jour et heure
     */
    private List<EpreuveResponse> getEpreuvesBySerieOrdered(String serieCode) {
        try {
            if (serieCode == null || serieCode.isEmpty()) {
                return List.of();
            }

            Query query = new Query();
            query.addCriteria(Criteria.where("serie.code").is(serieCode));

            // Trier par date réelle du jour puis par ordre de l'heure
            Sort sort = Sort.by(
                    Sort.Order.asc("jourDebut.date"),
                    Sort.Order.asc("heureDebut.ordre")
            );
            query.with(sort);

            List<Epreuve> epreuves = mongoTemplate.find(query, Epreuve.class, EPREUVE_COLLECTION);

            return epreuves.stream()
                    .map(epreuveMapper::toResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des épreuves", e);
            return List.of();
        }
    }

    /**
     * Convertit une liste de candidats en réponses avec leurs épreuves
     */
    private List<CandidatFinisResponse> toResponsesWithEpreuves(List<CandidatFinis> candidats) {
        return candidats.stream()
                .map(candidat -> {
                    CandidatFinisResponse response = candidatFinisMapper.toResponse(candidat);
                    List<EpreuveResponse> epreuves = getEpreuvesBySerieOrdered(candidat.getSerie());
                    response.setEpreuves(epreuves);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Convertit une page de candidats en page de réponses avec leurs épreuves
     */
    private Page<CandidatFinisResponse> toPageWithEpreuves(Page<CandidatFinis> page) {
        List<CandidatFinisResponse> responses = toResponsesWithEpreuves(page.getContent());
        return new PageImpl<>(responses, page.getPageable(), page.getTotalElements());
    }

    /**
     * Construction de la requête avec tous les filtres
     */
    private Query buildFilterQuery(
            String keyword,
            String serie,
            String numeroDossier,
            String jury,
            String typeCandidat,
            String statutResultat,
            String sexe,
            String etablissementCode,
            String nationalite) {

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (StringUtils.hasText(keyword)) {
            String searchPattern = ".*" + keyword.trim() + ".*";
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("prenoms").regex(searchPattern, "i"),
                    Criteria.where("nom").regex(searchPattern, "i"),
                    Criteria.where("numeroTable").regex(searchPattern, "i"),
                    Criteria.where("numeroDossier").regex(searchPattern, "i"),
                    Criteria.where("serie").regex(searchPattern, "i"),
                    Criteria.where("jury").regex(searchPattern, "i"),
                    Criteria.where("nationalite").regex(searchPattern, "i"),
                    Criteria.where("typeCandidat").regex(searchPattern, "i")
            );
            criteriaList.add(keywordCriteria);
        }

        if (StringUtils.hasText(serie)) {
            criteriaList.add(Criteria.where("serie").is(serie));
        }

        if (StringUtils.hasText(jury)) {
            criteriaList.add(Criteria.where("jury").is(jury));
        }

        if (StringUtils.hasText(jury)) {
            criteriaList.add(Criteria.where("numeroDossier").is(numeroDossier));
        }

        if (StringUtils.hasText(typeCandidat)) {
            criteriaList.add(Criteria.where("typeCandidat").is(typeCandidat));
        }

        if (StringUtils.hasText(statutResultat)) {
            criteriaList.add(Criteria.where("statutResultat").is(statutResultat));
        }
        if (StringUtils.hasText(etablissementCode)) {
            criteriaList.add(Criteria.where("etablissement.code").is(etablissementCode));
        }

        if (StringUtils.hasText(sexe)) {
            criteriaList.add(Criteria.where("sexe").is(sexe));
        }

        if (StringUtils.hasText(nationalite)) {
            criteriaList.add(Criteria.where("nationalite").regex(".*" + nationalite + ".*", "i"));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return query;
    }

    // ==================== MÉTHODES CRUD DE BASE ====================

    @Override
    @Transactional
    public CandidatFinisResponse create(CandidatFinisRequest request) {
        log.info("Creating new candidat with numeroTable: {}", request.getNumeroTable());

        if (request.getNumeroTable() != null &&
                candidatFinisRepository.existsByNumeroTable(request.getNumeroTable())) {
            throw new DuplicateResourceException("Candidat with numero table " +
                    request.getNumeroTable() + " already exists");
        }

        CandidatFinis entity = candidatFinisMapper.toEntity(request);
        CandidatFinis savedEntity = candidatFinisRepository.save(entity);
        log.info("Candidat created successfully with id: {}", savedEntity.getId());

        CandidatFinisResponse response = candidatFinisMapper.toResponse(savedEntity);
        List<EpreuveResponse> epreuves = getEpreuvesBySerieOrdered(savedEntity.getSerie());
        response.setEpreuves(epreuves);

        return response;
    }
    @Override
    @Transactional
    public CandidatFinisResponse update(String id, CandidatFinisRequest request) {
        log.info("Updating candidat with id: {}", id);

        CandidatFinis existingEntity = candidatFinisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat not found with id: " + id));

        // Vérifier les doublons de numéro de table
        if (request.getNumeroTable() != null &&
                !request.getNumeroTable().equals(existingEntity.getNumeroTable()) &&
                candidatFinisRepository.existsByNumeroTable(request.getNumeroTable())) {
            throw new DuplicateResourceException("Candidat with numero table " +
                    request.getNumeroTable() + " already exists");
        }

        // Sauvegarder les anciennes valeurs pour le cache et la suppression du PDF
        String oldNumeroTable = existingEntity.getNumeroTable();
        String oldCentreCode = existingEntity.getCentreEcrit().getCode();
        String oldCodeEtab = existingEntity.getEtablissement() != null ?
                existingEntity.getEtablissement().getCode() : null;
        String oldDateNaissance = existingEntity.getDateNaissance() != null ?
                formatDate(existingEntity.getDateNaissance()) : null;
        // Mettre à jour
        candidatFinisMapper.updateEntity(existingEntity, request);
        CandidatFinis updatedEntity = candidatFinisRepository.save(existingEntity);
        log.info("✅ Candidat mis à jour: id={}, numéro table={}", updatedEntity.getId(), updatedEntity.getNumeroTable());

        // ===== TOUJOURS VIDER LE CACHE =====
        if (oldCodeEtab != null && oldNumeroTable != null && oldDateNaissance != null) {
            convocationReactiveService.evictCache(oldCodeEtab, oldNumeroTable, oldDateNaissance)
                    .subscribe();
        }
        // Si le numéro de table a changé, vider aussi le cache avec le nouveau numéro (au cas où)
        if (!oldNumeroTable.equals(updatedEntity.getNumeroTable())) {
            String newCodeEtab = updatedEntity.getEtablissement() != null ?
                    updatedEntity.getEtablissement().getCode() : null;
            String newDateNaissance = updatedEntity.getDateNaissance() != null ?
                    formatDate(updatedEntity.getDateNaissance()) : null;
            if (newCodeEtab != null && newDateNaissance != null) {
                convocationReactiveService.evictCache(newCodeEtab, updatedEntity.getNumeroTable(), newDateNaissance)
                        .subscribe();
            }
        }

        // ===== TOUJOURS REGENERER LE PDF =====
        regenerateConvocation(updatedEntity, oldNumeroTable, oldCentreCode);
        return candidatFinisMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Deleting candidat with id: {}", id);

        if (!candidatFinisRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidat not found with id: " + id);
        }

        candidatFinisRepository.deleteById(id);
        log.info("Candidat deleted successfully with id: {}", id);
    }

    @Override
    public CandidatFinisResponse getById(String id) {
        log.info("Fetching candidat with id: {}", id);

        CandidatFinis entity = candidatFinisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat not found with id: " + id));

        CandidatFinisResponse response = candidatFinisMapper.toResponse(entity);
        List<EpreuveResponse> epreuves = getEpreuvesBySerieOrdered(entity.getSerie());
        response.setEpreuves(epreuves);

        return response;
    }

    // ==================== MÉTHODES DE LISTE AVEC FILTRES ET ÉPREUVES ====================

    @Override
    public PageResponse<CandidatFinisResponse> getAll(Pageable pageable) {
        log.info("Fetching all candidats with epreuves, pagination: {}", pageable);
        return getWithFilters(null, null, null, null, null, null, null,null, null,pageable);
    }

    @Override
    public PageResponse<CandidatFinisResponse> getWithFilters(
            String keyword,
            String serie,
            String jury,
            String numeroDossier,
            String typeCandidat,
            String statutResultat,
            String sexe,
            String nationalite,
            String etablissementCode,
            Pageable pageable) {

        log.info("Fetching candidats with filters and epreuves - keyword: {}, serie: {}, etablissementCode: {},jury: {}, type: {}, statut: {}, sexe: {}, nationalite: {}",
                keyword, serie,etablissementCode, jury, typeCandidat, statutResultat, sexe, nationalite);

        Query query = buildFilterQuery(keyword, serie, jury, numeroDossier, typeCandidat,
                statutResultat, sexe, nationalite, etablissementCode);
        long total = mongoTemplate.count(query, CandidatFinis.class, COLLECTION_NAME);
        query.with(pageable);

        List<CandidatFinis> candidats = mongoTemplate.find(query, CandidatFinis.class, COLLECTION_NAME);
        Page<CandidatFinis> page = new PageImpl<>(candidats, pageable, total);
        Page<CandidatFinisResponse> responsePage = toPageWithEpreuves(page);

        return toPageResponse(responsePage);
    }

    @Override
    public PageResponse<CandidatFinisResponse> search(String keyword, Pageable pageable) {
        log.info("Searching candidats with keyword: {}, pageable: {}", keyword, pageable);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll(pageable);
        }

        return getWithFilters(keyword.trim(), null, null, null, null, null, null,null,null, pageable);
    }

    @Override
    public PageResponse<CandidatFinisResponse> getBySerie(String serieCode, Pageable pageable) {
        log.info("Fetching candidats by serie with epreuves: {}", serieCode);
        return getWithFilters(null, serieCode, null, null, null, null, null,null,null, pageable);
    }

    @Override
    public List<String> getAllDistinctSeries() {
        log.info("Fetching all distinct series from candidats");
        return mongoTemplate.findDistinct("serie", CandidatFinis.class, String.class);
    }
    @Override
    public List<String> getMyDistinctSeries() {
        log.info("Fetching all distinct series from candidats of connected user establishment");

        try {
            // Récupérer l'établissement de l'utilisateur connecté
            Etablissement etablissement = getEtablissementUtilisateurConnecte();
            String etablissementId = etablissement.getId();
            log.info("Établissement connecté: {} (ID: {})", etablissement.getName(), etablissementId);

            // Convertir l'ID en ObjectId si nécessaire
            ObjectId objectId;
            try {
                objectId = new ObjectId(etablissementId);
            } catch (IllegalArgumentException e) {
                log.error("ID d'établissement invalide: {}", etablissementId);
                return List.of();
            }

            // Construire la requête pour filtrer par établissement
            Query query = new Query();
            query.addCriteria(Criteria.where("etablissement._id").is(objectId));

            // Récupérer les valeurs distinctes du champ "serie"
            List<String> series = mongoTemplate.findDistinct(query, "serie", CandidatFinis.class, String.class);

            // Filtrer les valeurs null ou vides et trier
            List<String> result = series.stream()
                    .filter(s -> s != null && !s.isEmpty())
                    .sorted()
                    .collect(Collectors.toList());

            log.info("Séries distinctes trouvées: {} - {}", result.size(), result);
            return result;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des séries distinctes de l'établissement: {}", e.getMessage());
            return List.of();
        }
    }


    @Override
    public PageResponse<CandidatFinisResponse> getByJury(String jury, Pageable pageable) {
        log.info("Fetching candidats by jury with epreuves: {}", jury);
        return getWithFilters(null, null, jury, null, null, null, null,null, null,pageable);
    }

    // ==================== MÉTHODES AVEC FILTRE ÉTABLISSEMENT ====================

    @Override
    public PageResponse<CandidatFinisResponse> getAllByUtilisateurConnecte(Pageable pageable) {
        log.info("Fetching all candidats of connected user establishment with epreuves");
        return getWithFiltersByUtilisateurConnecte(null, null, null, null, null, null, null,null,null, pageable);
    }

    @Override
    public PageResponse<CandidatFinisResponse> getWithFiltersByUtilisateurConnecte(
            String keyword,
            String serie,
            String jury,
            String typeCandidat,
            String statutResultat,
            String numeroDossier,
            String sexe,
            String nationalite,
            String etablissementCode,
            Pageable pageable) {

        log.info("Fetching candidats of connected user establishment with filters and epreuves");

        Etablissement etablissement = getEtablissementUtilisateurConnecte();

        Query query =  buildFilterQuery(keyword, serie, jury, typeCandidat, numeroDossier,
                statutResultat, sexe, nationalite, etablissementCode);
        query.addCriteria(Criteria.where("etablissement.id").is(etablissement.getId()));

        long total = mongoTemplate.count(query, CandidatFinis.class, COLLECTION_NAME);
        query.with(pageable);

        List<CandidatFinis> candidats = mongoTemplate.find(query, CandidatFinis.class, COLLECTION_NAME);
        Page<CandidatFinis> page = new PageImpl<>(candidats, pageable, total);
        Page<CandidatFinisResponse> responsePage = toPageWithEpreuves(page);

        return toPageResponse(responsePage);
    }

    @Override
    public PageResponse<CandidatFinisResponse> getBySerieByUtilisateurConnecte(String serieCode, Pageable pageable) {
        log.info("Fetching candidats by serie of connected user establishment with epreuves: {}", serieCode);
        return getWithFiltersByUtilisateurConnecte(null, serieCode, null, null, null, null, null,null,null, pageable);
    }

    @Override
    public PageResponse<CandidatFinisResponse> getByJuryByUtilisateurConnecte(String jury, Pageable pageable) {
        log.info("Fetching candidats by jury of connected user establishment with epreuves: {}", jury);
        return getWithFiltersByUtilisateurConnecte(null, null, jury, null, null, null, null,null,null, pageable);
    }

    @Override
    public CandidatFinisResponse getByIdByUtilisateurConnecte(String id) {
        log.info("Fetching candidat by id with establishment verification and epreuves: id={}", id);

        Etablissement etablissement = getEtablissementUtilisateurConnecte();

        CandidatFinis candidat = candidatFinisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'id: " + id));

        if (candidat.getEtablissement() == null ||
                !candidat.getEtablissement().getId().equals(etablissement.getId())) {
            throw new BusinessResourceException("unauthorized",
                    "Vous n'avez pas accès à ce candidat", HttpStatus.FORBIDDEN);
        }

        CandidatFinisResponse response = candidatFinisMapper.toResponse(candidat);
        List<EpreuveResponse> epreuves = getEpreuvesBySerieOrdered(candidat.getSerie());
        response.setEpreuves(epreuves);

        return response;
    }
    // CandidatFinisServiceImpl.java

    /**
     * Exporte les candidats vers un fichier Excel
     */
    /**
     * Exporte TOUS les candidats de l'établissement connecté vers Excel
     */
    @Override
    public ByteArrayInputStream exportAllCandidatsToExcel() {
        log.info("📊 Export de TOUS les candidats vers Excel");

        Etablissement etablissement = getEtablissementUtilisateurConnecte();

        Query query = new Query();
        query.addCriteria(Criteria.where("etablissement.id").is(etablissement.getId()));

        // ===== AJOUT DU TRI =====
        Sort sort = Sort.by("serie").ascending()
                .and(Sort.by("nom").ascending())
                .and(Sort.by("prenoms").ascending());

        query.with(sort);

        List<CandidatFinis> candidats = mongoTemplate.find(query, CandidatFinis.class, COLLECTION_NAME);

        log.info("📊 {} candidats trouvés pour l'export (triés par série, nom, prénoms)", candidats.size());

        List<CandidatFinisResponse> responses = toResponsesWithEpreuves(candidats);

        return candidatExportService.generateCandidatsExcel(responses);
    }
    @Override
    public List<CandidatFinisResponse> getAllByUtilisateurConnecteNoPagination(String serie) {

        log.info("📥 Récupération de tous les candidats (sans pagination) pour série: {}", serie);

        Etablissement etablissement = getEtablissementUtilisateurConnecte();

        Query query = new Query();

        // ✅ Conversion en ObjectId
        ObjectId objectId;
        try {
            objectId = new ObjectId(etablissement.getId());
        } catch (IllegalArgumentException e) {
            log.error("❌ ID établissement invalide: {}", etablissement.getId());
            return List.of();
        }

        // ✅ BON champ Mongo
        query.addCriteria(Criteria.where("etablissement._id").is(objectId));

        // ✅ Filtre série (optionnel)
        if (StringUtils.hasText(serie)) {
            query.addCriteria(Criteria.where("serie").is(serie));
        }

        // ===== TRI UNIQUEMENT PAR NOM ET PRÉNOMS =====
        Sort sort = Sort.by("nom").ascending()
                .and(Sort.by("prenoms").ascending());

        query.with(sort);

        List<CandidatFinis> candidats = mongoTemplate.find(query, CandidatFinis.class, COLLECTION_NAME);

        log.info("✅ {} candidats récupérés (triés par nom, prénoms)", candidats.size());

        // ⚡ OPTIMISATION (important)
        List<EpreuveResponse> epreuves = getEpreuvesBySerieOrdered(serie);

        return candidats.stream()
                .map(candidat -> {
                    CandidatFinisResponse response = candidatFinisMapper.toResponse(candidat);
                    response.setEpreuves(epreuves);
                    return response;
                })
                .toList();
    }
    @Override
    public List<CandidatFinisResponse> getAllByUtilisateurConnecte() {

        log.info("📥 Récupération de tous les candidats (sans pagination)");

        Etablissement etablissement = getEtablissementUtilisateurConnecte();

        Query query = new Query();

        // ✅ Conversion en ObjectId
        ObjectId objectId;
        try {
            objectId = new ObjectId(etablissement.getId());
        } catch (IllegalArgumentException e) {
            log.error("❌ ID établissement invalide: {}", etablissement.getId());
            return List.of();
        }

        // ✅ BON champ Mongo
        query.addCriteria(Criteria.where("etablissement._id").is(objectId));


        List<CandidatFinis> candidats = mongoTemplate.find(query, CandidatFinis.class, COLLECTION_NAME);

        log.info("✅ {} candidats récupérés", candidats.size());


        return candidats.stream()
                .map(candidatFinisMapper::toResponse)
                .toList();
    }
    @Override
    public Jour getJourEPS() {
        log.info("📅 Récupération du jour EPS avec le code: JEPS");
        try {
            // Utiliser le code "JEPS" pour trouver le jour EPS
            Query query = new Query();
            query.addCriteria(Criteria.where("code").is("JEPS"));

            Jour jourEPS = mongoTemplate.findOne(query, Jour.class, "jour");

            if (jourEPS == null) {
                log.warn("⚠️ Aucun jour trouvé avec le code JEPS");
                return null;
            }
            log.info("✅ Jour EPS trouvé: {} - {} - date: {}",
                    jourEPS.getCode(), jourEPS.getName(), jourEPS.getDate());

            return jourEPS;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération du jour EPS: {}", e.getMessage());
            return null;
        }
    }
    /**
     * Régénère la convocation après modification
     */
    private String formatDate(String dateString) {
        if (dateString == null) return null;
        try {
            LocalDate date = LocalDate.parse(dateString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return date.format(formatter);
        } catch (Exception e) {
            return dateString;
        }
    }
    /**
     * Régénère la convocation PDF (remplace toujours l'ancien)
     */
    private void regenerateConvocation(CandidatFinis candidat, String oldNumeroTable, String oldCentreCode) {
        try {
            String numeroTable = candidat.getNumeroTable();
            String centreCode = candidat.getCentreEcrit() != null ? candidat.getCentreEcrit().getCode() : "unknown";

            log.info("📄 Régénération PDF pour: {}", numeroTable);

            // Générer le nouveau PDF
            byte[] pdfBytes = convocationPdfService.generateConvocation(numeroTable).block();

            if (pdfBytes != null) {
                // ✅ Utiliser storageConfig pour le chemin
                Path centrePath = storageConfig.getBasePath().resolve(sanitizeCode(centreCode));
                Files.createDirectories(centrePath);

                Path targetPath = centrePath.resolve(numeroTable + ".pdf");
                Files.write(targetPath, pdfBytes);
                log.info("✅ PDF sauvegardé: {}", targetPath);

                // Si le numéro de table a changé, supprimer l'ancien PDF
                if (oldNumeroTable != null && !oldNumeroTable.equals(numeroTable) && oldCentreCode != null) {
                    Path oldPath = storageConfig.getBasePath().resolve(sanitizeCode(oldCentreCode)).resolve(oldNumeroTable + ".pdf");
                    Files.deleteIfExists(oldPath);
                    log.info("🗑️ Ancien PDF supprimé: {}", oldNumeroTable);
                }
            } else {
                log.error("❌ Échec génération PDF pour: {}", numeroTable);
            }

        } catch (Exception e) {
            log.error("❌ Erreur régénération PDF: {}", e.getMessage(), e);
        }
    }

    private String sanitizeCode(String code) {
        if (code == null) return "unknown";
        return code.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
    @Override
    public Mono<String> regenerateConvocation(String numeroTable) {
        log.info("🔄 Régénération manuelle pour: {}", numeroTable);

        return Mono.fromCallable(() -> candidatFinisRepository.findByNumeroTable(numeroTable))
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new BusinessResourceException(
                                "not-found", "Candidat non trouvé avec le numéro: " + numeroTable, HttpStatus.NOT_FOUND));
                    }
                    CandidatFinis candidat = optional.get();

                    String centreCode = candidat.getCentreEcrit() != null ?
                            candidat.getCentreEcrit().getCode() : "unknown";
                    String numeroTableCourant = candidat.getNumeroTable();

                    log.info("📄 Génération du PDF pour: {} - Centre: {}", numeroTableCourant, centreCode);

                    return convocationPdfService.generateConvocation(numeroTableCourant)
                            .flatMap(pdfBytes -> {
                                try {
                                    Path centrePath = storageConfig.getBasePath().resolve(sanitizeCode(centreCode));
                                    Files.createDirectories(centrePath);

                                    Path targetPath = centrePath.resolve(numeroTableCourant + ".pdf");
                                    Files.write(targetPath, pdfBytes);

                                    log.info("✅ PDF sauvegardé: {} ({} bytes)", targetPath, pdfBytes.length);

                                    return Mono.just(targetPath.toString());
                                } catch (Exception e) {
                                    log.error("❌ Erreur sauvegarde: {}", e.getMessage());
                                    return Mono.error(new BusinessResourceException(
                                            "save-error", "Erreur lors de la sauvegarde du PDF: " + e.getMessage(),
                                            HttpStatus.INTERNAL_SERVER_ERROR));
                                }
                            });
                });
    }
}