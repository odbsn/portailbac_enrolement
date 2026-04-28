package com.officedubac.project.module.epreuve;

import com.fasterxml.jackson.core.type.TypeReference;
import com.officedubac.project.module.candidatFinis.CandidatFinis;
import com.officedubac.project.module.convocations.CacheService;
import com.officedubac.project.module.epreuve.dto.EpreuveResponse;
import com.officedubac.project.module.jour.Jour;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EpreuveReactiveService {

    private final ReactiveMongoTemplate mongo;
    private final CacheService cache;

    private static final String JOUR_EPS_CACHE_KEY = "jour:eps";

    /**
     * CACHE FIRST STRATEGY
     */
    public Mono<List<EpreuveResponse>> getBySerie(String serieCode) {

        if (serieCode == null || serieCode.isBlank()) {
            return Mono.just(List.of());
        }

        String key = "epreuves:" + serieCode;

        return cache.get(
                        key,
                        new TypeReference<List<EpreuveResponse>>() {}
                )
                .switchIfEmpty(
                        fetchFromMongo(serieCode)
                                .flatMap(list -> cache.set(key, list))
                );
    }

    /**
     * MONGO QUERY
     */
    private Mono<List<EpreuveResponse>> fetchFromMongo(String serieCode) {

        Query query = new Query()
                .addCriteria(Criteria.where("serie.code").is(serieCode))
                .with(Sort.by(
                        Sort.Order.asc("jourDebut.date"),
                        Sort.Order.asc("heureDebut.ordre")
                ));

        return mongo.find(query, Epreuve.class)
                .map(this::mapToEpreuveResponse)
                .collectList()
                .doOnNext(list ->
                        log.info("✅ Mongo fetch {} épreuves pour série {}", list.size(), serieCode)
                );
    }

    /**
     * MAPPING SAFE
     */
    private EpreuveResponse mapToEpreuveResponse(Epreuve epreuve) {

        return EpreuveResponse.builder()
                .id(epreuve.getId())
                .matiere(epreuve.getMatiere())
                .serie(epreuve.getSerie())
                .coefficient(epreuve.getCoefficient())
                .autorisation(epreuve.getAutorisation())
                .estDominant(epreuve.getEstDominant())
                .nombrePoints(epreuve.getNombrePoints())
                .jourDebut(epreuve.getJourDebut())
                .heureDebut(epreuve.getHeureDebut())
                .duree(epreuve.getDuree())
                .type(epreuve.getType())
                .build();
    }

    /**
     * FIND CANDIDAT
     */
    public Mono<CandidatFinis> findByNumeroTable(String numeroTable) {

        return mongo.findOne(
                Query.query(Criteria.where("numeroTable").is(numeroTable)),
                CandidatFinis.class
        );
    }

    /**
     * GET JOUR EPS WITH CACHE FIRST STRATEGY
     */
    public Mono<Jour> getJourEPS() {
        log.info("📅 Récupération du jour EPS avec le code: JEPS");

        return cache.get(JOUR_EPS_CACHE_KEY, new TypeReference<Jour>() {})
                .switchIfEmpty(fetchJourEPSFromMongo()
                        .flatMap(jour -> cache.set(JOUR_EPS_CACHE_KEY, jour))
                )
                .doOnNext(jour -> log.info("✅ Jour EPS trouvé: {} - {} - date: {}",
                        jour.getCode(), jour.getName(), jour.getDate()))
                .doOnError(error -> log.error("❌ Erreur lors de la récupération du jour EPS: {}", error.getMessage()));
    }

    /**
     * FETCH JOUR EPS FROM MONGO
     */
    private Mono<Jour> fetchJourEPSFromMongo() {
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is("JEPS"));

        return mongo.findOne(query, Jour.class)
                .doOnNext(jour -> log.info("✅ Mongo fetch jour EPS: {}", jour.getName()))
                .doOnSuccess(jour -> {
                    if (jour == null) {
                        log.warn("⚠️ Aucun jour trouvé avec le code JEPS dans MongoDB");
                    }
                });
    }
}