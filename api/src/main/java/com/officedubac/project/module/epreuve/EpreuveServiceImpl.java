package com.officedubac.project.module.epreuve;

import com.officedubac.project.exception.DuplicateResourceException;
import com.officedubac.project.exception.ResourceNotFoundException;
import com.officedubac.project.models.Matiere;
import com.officedubac.project.module.epreuve.dto.EpreuveRequest;
import com.officedubac.project.module.epreuve.dto.EpreuveResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EpreuveServiceImpl implements EpreuveService {

    private final EpreuveRepository epreuveRepository;
    private final MongoTemplate mongoTemplate;
    private final EpreuveMapper epreuveMapper;

    private static final String COLLECTION_NAME = "epreuve";

    @Override
    public EpreuveResponse create(EpreuveRequest request) {
        log.info("Création d'une nouvelle épreuve: matiere={}, serie={}, type={}",
                request.getMatiere(), request.getSerie(), request.getType());

        // Vérifier si l'épreuve existe déjà
        if (epreuveRepository.existsByMatiereIdAndSerieIdAndType(
                request.getMatiere(), request.getSerie(), request.getType())) {
            throw new DuplicateResourceException(
                    "Une épreuve existe déjà pour cette matière, série et type"
            );
        }

        Epreuve entity = epreuveMapper.toEntity(request);
        Epreuve savedEntity = epreuveRepository.save(entity);

        log.info("Épreuve créée avec succès: id={}", savedEntity.getId());
        return epreuveMapper.toResponse(savedEntity);
    }

    @Override
    public EpreuveResponse update(String id, EpreuveRequest request) {
        log.info("Mise à jour de l'épreuve: id={}", id);

        Epreuve existingEntity = epreuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Épreuve non trouvée avec l'id: " + id));

        // Vérifier si la nouvelle combinaison existe déjà (pour un autre ID)
        if (!existingEntity.getMatiere().getId().equals(request.getMatiere()) ||
                !existingEntity.getSerie().getId().equals(request.getSerie()) ||
                !existingEntity.getType().equals(request.getType())) {

            if (epreuveRepository.existsByMatiereIdAndSerieIdAndType(
                    request.getMatiere(), request.getSerie(), request.getType())) {
                throw new DuplicateResourceException(
                        "Une épreuve existe déjà pour cette matière, série et type"
                );
            }
        }

        epreuveMapper.updateEntity(existingEntity, request);
        Epreuve updatedEntity = epreuveRepository.save(existingEntity);

        log.info("Épreuve mise à jour avec succès: id={}", updatedEntity.getId());
        return epreuveMapper.toResponse(updatedEntity);
    }

    @Override
    public void delete(String id) {
        log.info("Suppression de l'épreuve: id={}", id);

        if (!epreuveRepository.existsById(id)) {
            throw new ResourceNotFoundException("Épreuve non trouvée avec l'id: " + id);
        }

        epreuveRepository.deleteById(id);
        log.info("Épreuve supprimée avec succès: id={}", id);
    }

    @Override
    public EpreuveResponse getById(String id) {
        log.info("Récupération de l'épreuve: id={}", id);

        Epreuve entity = epreuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Épreuve non trouvée avec l'id: " + id));

        return epreuveMapper.toResponse(entity);
    }

    @Override
    public Page<EpreuveResponse> getAll(Pageable pageable) {
        log.info("Récupération de toutes les épreuves avec pagination: {}", pageable);
        return getWithFilters(null, null, null, null, null, null, pageable);
    }

    @Override
    public Page<EpreuveResponse> search(String keyword, Pageable pageable) {
        log.info("Recherche d'épreuves avec mot-clé: {}, pageable: {}", keyword, pageable);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll(pageable);
        }

        return getWithFilters(keyword.trim(), null, null, null, null, null, pageable);
    }

    /**
     * Méthode principale avec tous les filtres possibles
     */
    @Override
    public Page<EpreuveResponse> getWithFilters(
            String keyword,
            String matiereId,
            String serieId,
            String type,
            Boolean autorisation,
            Boolean estDominant,
            Pageable pageable) {

        log.info("Récupération des épreuves avec filtres - keyword: {}, matiereId: {}, serieId: {}, type: {}, autorisation: {}, estDominant: {}",
                keyword, matiereId, serieId, type, autorisation, estDominant);

        // Construire la requête avec tous les filtres
        Query query = buildFilterQuery(keyword, matiereId, serieId, type, autorisation, estDominant);

        // Compter le nombre total
        long total = mongoTemplate.count(query, Epreuve.class, COLLECTION_NAME);

        // Appliquer la pagination et le tri
        query.with(pageable);

        // Exécuter la requête
        List<Epreuve> epreuves = mongoTemplate.find(query, Epreuve.class, COLLECTION_NAME);

        // Créer la page
        Page<Epreuve> page = new PageImpl<>(epreuves, pageable, total);

        return page.map(epreuveMapper::toResponse);
    }

    /**
     * Construction de la requête avec tous les filtres
     */
    private Query buildFilterQuery(
            String keyword,
            String matiereId,
            String serieId,
            String type,
            Boolean autorisation,
            Boolean estDominant) {

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // 1. Filtre par mot-clé (recherche dans plusieurs champs)
        if (StringUtils.hasText(keyword)) {
            String searchPattern = ".*" + keyword.trim() + ".*";
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("matiere.code").regex(searchPattern, "i"),
                    Criteria.where("matiere.name").regex(searchPattern, "i"),
                    Criteria.where("serie.code").regex(searchPattern, "i"),
                    Criteria.where("serie.name").regex(searchPattern, "i"),
                    Criteria.where("type").regex(searchPattern, "i"),
                    Criteria.where("duree").regex(searchPattern, "i")
            );
            criteriaList.add(keywordCriteria);
        }

        // 2. Filtre par matière (par ID)
        if (StringUtils.hasText(matiereId)) {
            criteriaList.add(Criteria.where("matiere._id").is(matiereId));
        }

        // 3. Filtre par série (par ID)
        if (StringUtils.hasText(serieId)) {
            criteriaList.add(Criteria.where("serie._id").is(serieId));
        }

        // 4. Filtre par type (Ecrit/Oral/Pratique)
        if (StringUtils.hasText(type)) {
            criteriaList.add(Criteria.where("type").is(type));
        }

        // 5. Filtre par autorisation
        if (autorisation != null) {
            criteriaList.add(Criteria.where("autorisation").is(autorisation));
        }

        // 6. Filtre par matière dominante
        if (estDominant != null) {
            criteriaList.add(Criteria.where("estDominant").is(estDominant));
        }

        // Combiner tous les critères
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return query;
    }
    /**
     * Met à jour directement en base sans charger toutes les entités
     */
    @Override
    @Transactional
    public int updateEpreuvesTypeDirectly() {
        log.info("Mise à jour directe des types pour LAFAC, LBFAC, EPS");

        long startTime = System.currentTimeMillis();

        // Récupérer les matières avec leurs ObjectId
        List<String> codesMatieres = Arrays.asList("LAFAC", "LBFAC", "EPS");
        List<Matiere> matieres = mongoTemplate.find(
                Query.query(Criteria.where("code").in(codesMatieres)),
                Matiere.class
        );

        if (matieres.isEmpty()) {
            log.warn("Aucune matière trouvée pour les codes: {}", codesMatieres);
            return 0;
        }

        // Récupérer les IDs des matières
        List<ObjectId> matiereIds = matieres.stream()
                .map(matiere -> new ObjectId(matiere.getId()))
                .collect(Collectors.toList());

        log.info("Matières trouvées: {}", matieres.stream()
                .map(m -> m.getCode() + " (" + m.getId() + ")")
                .collect(Collectors.joining(", ")));

        // Mise à jour directe avec le bon format de requête
        Query query = Query.query(Criteria.where("matiere._id").in(matiereIds));
        org.springframework.data.mongodb.core.query.Update update =
                new org.springframework.data.mongodb.core.query.Update()
                        .set("type", "Ecr./Prat.");

        var result = mongoTemplate.updateMulti(query, update, Epreuve.class);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("✅ {} épreuves mises à jour en {} ms", result.getModifiedCount(), elapsed);

        return (int) result.getModifiedCount();
    }
}