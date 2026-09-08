package com.officedubac.project.module.bacheliersToCampusen;

import com.officedubac.project.module.bacheliersToCampusen.dto.SerieStat;
import com.officedubac.project.module.bacheliersToCampusen.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AdmisService
{

    private static final int TAILLE_MAX_PAGE = 500;

    private final BacheliersAdmisRepository bacheliersAdmisRepository;
    private final MongoTemplate mongoTemplate;

    /** Liste simple paginée, sans notion de session : les données sont sessionnières
     *  (une seule session en base à la fois, purgée après exploitation) — donc pas besoin
     *  de filtrer par année, comme la liste de nouveauBachelier. */
    public Page<BacheliersToCampusen> findAllPaginated(int page, int size, String search) {
        Query query = new Query();
        if (search != null && !search.isBlank()) {
            String regex = Pattern.quote(search.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("nom").regex(regex, "i"),
                    Criteria.where("prenoms").regex(regex, "i"),
                    Criteria.where("numeroTable").regex(regex, "i"),
                    Criteria.where("telephone").regex(regex, "i")
            ));
        }
        long total = mongoTemplate.count(query, BacheliersToCampusen.class);
        Pageable pageable = pageable(page, size);
        query.with(pageable);
        List<BacheliersToCampusen> contenu = mongoTemplate.find(query, BacheliersToCampusen.class);
        return new org.springframework.data.domain.PageImpl<>(contenu, pageable, total);
    }

    public Page<BacheliersToCampusen> parAnnee(int annee, int page, int taille) {
        verifierAnnee(annee);
        return bacheliersAdmisRepository.findByAnnee(annee, pageable(page, taille));
    }

    public Page<BacheliersToCampusen> parAnneeEtSerie(int annee, String serie, int page, int taille) {
        verifierAnnee(annee);
        Page<BacheliersToCampusen> resultat = bacheliersAdmisRepository.findByAnneeAndSerie(annee, serie, pageable(page, taille));
        if (resultat.getTotalElements() == 0) {
            throw new NotFoundException("Aucun admis pour la série '" + serie + "' en " + annee
                    + ". Séries disponibles : " + seriesDisponibles(annee));
        }
        return resultat;
    }

    public BacheliersToCampusen parNumeroTable(int annee, String numeroTable) {
        return bacheliersAdmisRepository.findFirstByAnneeAndNumeroTable(annee, numeroTable)
                .orElseThrow(() -> new NotFoundException(
                        "Aucun admis avec le n° de table " + numeroTable + " en " + annee));
    }

    /** Recherche paginée au sein d'une session (annee) : texte libre (nom, prénoms, n° table,
     *  téléphone) combiné à un filtre optionnel de série — même logique que la recherche
     *  serveur de nouveauBachelier. */
    public Page<BacheliersToCampusen> rechercher(int annee, String search, String serie, int page, int taille) {
        verifierAnnee(annee);

        List<Criteria> criterias = new ArrayList<>();
        criterias.add(Criteria.where("annee").is(annee));
        if (serie != null && !serie.isBlank()) {
            criterias.add(Criteria.where("serie").is(serie));
        }
        if (search != null && !search.isBlank()) {
            String regex = Pattern.quote(search.trim());
            criterias.add(new Criteria().orOperator(
                    Criteria.where("nom").regex(regex, "i"),
                    Criteria.where("prenoms").regex(regex, "i"),
                    Criteria.where("numeroTable").regex(regex, "i"),
                    Criteria.where("telephone").regex(regex, "i")
            ));
        }
        Criteria criteria = new Criteria().andOperator(criterias.toArray(new Criteria[0]));

        Pageable pageable = pageable(page, taille);
        Query query = Query.query(criteria).with(pageable);
        List<BacheliersToCampusen> contenu = mongoTemplate.find(query, BacheliersToCampusen.class);
        long total = mongoTemplate.count(Query.query(criteria), BacheliersToCampusen.class);
        return new org.springframework.data.domain.PageImpl<>(contenu, pageable, total);
    }

    public List<SerieStat> statistiquesSeries(int annee) {
        verifierAnnee(annee);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("annee").is(annee)),
                Aggregation.group("serie").count().as("nombreAdmis"),
                Aggregation.sort(Sort.Direction.ASC, "_id"));
        AggregationResults<Document> resultats = mongoTemplate.aggregate(agg, "bacheliers_to_campusen", Document.class);
        return resultats.getMappedResults().stream()
                .map(d -> new SerieStat(d.getString("_id"), ((Number) d.get("nombreAdmis")).longValue()))
                .toList();
    }

    public List<Integer> anneesDisponibles() {
        return mongoTemplate.findDistinct(new Query(), "annee", BacheliersToCampusen.class, Integer.class)
                .stream().sorted().toList();
    }

    private List<String> seriesDisponibles(int annee) {
        return mongoTemplate.findDistinct(
                Query.query(Criteria.where("annee").is(annee)), "serie", BacheliersToCampusen.class, String.class)
                .stream().sorted().toList();
    }

    private void verifierAnnee(int annee) {
        if (!bacheliersAdmisRepository.existsByAnnee(annee)) {
            throw new NotFoundException("Aucune donnée pour l'année " + annee
                    + ". Années disponibles : " + anneesDisponibles());
        }
    }

    private Pageable pageable(int page, int taille) {
        if (page < 0) page = 0;
        if (taille < 1) taille = 50;
        if (taille > TAILLE_MAX_PAGE) taille = TAILLE_MAX_PAGE;
        return PageRequest.of(page, taille, Sort.by("serie", "numeroTable"));
    }
}
