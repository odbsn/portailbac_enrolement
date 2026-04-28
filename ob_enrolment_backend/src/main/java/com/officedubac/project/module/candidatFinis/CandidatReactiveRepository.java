package com.officedubac.project.module.candidatFinis;

import com.officedubac.project.module.candidatFinis.dto.ConvocationProjection;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CandidatReactiveRepository extends ReactiveMongoRepository<CandidatFinis, String> {

    // Requête optimisée avec index
    @Query("{ 'numeroTable': ?0, 'dateNaissance': ?1, 'etablissement.code': ?2 }")
    Mono<ConvocationProjection> findByNumeroTableAndDateNaissanceAndEtablissement_Code(
            String numeroTable,
            String dateNaissance,
            String codeEtab
    );
}