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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdmisService
{

    private static final int TAILLE_MAX_PAGE = 500;

    private final BacheliersAdmisRepository bacheliersAdmisRepository;
    private final MongoTemplate mongoTemplate;

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

    public Page<BacheliersToCampusen> rechercher(int annee, String nom, String prenom, String serie, int page, int taille) {
        verifierAnnee(annee);
        Criteria criteria = Criteria.where("annee").is(annee);
        if (serie != null && !serie.isBlank()) criteria = criteria.and("serie").is(serie);
        if (nom != null && !nom.isBlank()) criteria = criteria.and("nom").regex(nom.trim(), "i");
        if (prenom != null && !prenom.isBlank()) criteria = criteria.and("prenoms").regex(prenom.trim(), "i");

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
        return mongoTemplate.findDistinct(new Query(), "annee", "candidats", Integer.class)
                .stream().sorted().toList();
    }

    private List<String> seriesDisponibles(int annee) {
        return mongoTemplate.findDistinct(
                Query.query(Criteria.where("annee").is(annee)), "serie", "candidats", String.class)
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
