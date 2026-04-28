package com.officedubac.project.module.candidatFinis;

import com.officedubac.project.module.candidatFinis.dto.ConvocationDTO;
import com.officedubac.project.module.candidatFinis.dto.ConvocationProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocationReactiveService {

    private final CandidatReactiveRepository repository;
    private final ReactiveRedisTemplate<String, ConvocationDTO> redisTemplate;
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private String buildCacheKey(String code, String num, String date) {
        return String.format("conv:%s:%s:%s", code, num, date);
    }

    /**
     * Construit toutes les clés de cache possibles pour un candidat
     */
    private String buildCacheKeyPattern(String code, String num, String date) {
        return String.format("conv:%s:%s:%s", code, num, date);
    }

    public Mono<ConvocationDTO> findConvocation(String codeEtab, String numeroTable, String dateNaissance) {
        String cacheKey = buildCacheKey(codeEtab, numeroTable, dateNaissance);

        return redisTemplate.opsForValue().get(cacheKey)
                .doOnSuccess(dto -> {
                    if (dto != null) {
                        log.debug("✅ Cache HIT - Key: {}", cacheKey);
                    } else {
                        log.debug("❌ Cache MISS - Key: {}", cacheKey);
                    }
                })
                .switchIfEmpty(Mono.defer(() ->
                        repository.findByNumeroTableAndDateNaissanceAndEtablissement_Code(
                                        numeroTable, dateNaissance, codeEtab)
                                .subscribeOn(Schedulers.boundedElastic())
                                .switchIfEmpty(Mono.error(new RuntimeException("Candidat non trouvé")))
                                .map(this::mapToDTO)
                                .flatMap(dto ->
                                        redisTemplate.opsForValue()
                                                .set(cacheKey, dto, CACHE_TTL)
                                                .doOnSuccess(success -> log.info("💾 Cached convocation for key: {}", cacheKey))
                                                .thenReturn(dto)
                                )
                ))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(error -> {
                    log.error("Error fetching convocation: {}", error.getMessage());
                    return Mono.error(new RuntimeException("Service temporairement indisponible"));
                });
    }

    /**
     * Vide le cache pour un candidat spécifique
     */
    public Mono<Void> evictCache(String codeEtab, String numeroTable, String dateNaissance) {
        String cacheKey = buildCacheKey(codeEtab, numeroTable, dateNaissance);
        return redisTemplate.opsForValue().delete(cacheKey)
                .doOnSuccess(deleted -> {
                    if (Boolean.TRUE.equals(deleted)) {
                        log.info("🗑️ Cache supprimé pour la clé: {}", cacheKey);
                    } else {
                        log.debug("Clé non trouvée dans le cache: {}", cacheKey);
                    }
                })
                .then();
    }

    /**
     * Vide le cache pour un candidat (version avec ancienne clé)
     */
    public Mono<Void> evictCacheByOldKey(String oldCodeEtab, String oldNumeroTable, String oldDateNaissance) {
        if (oldCodeEtab != null && oldNumeroTable != null && oldDateNaissance != null) {
            return evictCache(oldCodeEtab, oldNumeroTable, oldDateNaissance);
        }
        return Mono.empty();
    }


    /**
     * Vide le cache pour tous les candidats (cas extrême)
     */
    public Mono<Void> evictAllCache() {
        return redisTemplate.keys("conv:*")
                .flatMap(redisTemplate::delete)
                .then()
                .doOnSuccess(v -> log.info("🗑️ Tous les caches des convocations ont été vidés"));
    }

    private ConvocationDTO mapToDTO(ConvocationProjection p) {
        return new ConvocationDTO(
                p.getPrenoms(),
                p.getNom(),
                p.getDateNaissance(),
                p.getLieuNaissance(),
                p.getNationalite(),
                p.getNumeroTable(),
                p.getJury(),
                p.getSerie(),
                p.getSexe(),
                p.getTypeCandidat(),
                p.getEps(),
                p.getEtablissementName(),
                p.getCodeEtab(),
                p.getCentreEcritName(),
                p.getCentreCode(),
                p.getCentreEcritParticulier(),
                p.getCentreActEPS() != null ? p.getCentreActEPS().getName() : null,
                p.getMo1(),
                p.getMo2(),
                p.getMo3(),
                p.getEf1(),
                p.getEf2(),
                p.getCentreMatFac1(),
                p.getLibMatFac1(),
                p.getCentreMatFac2(),
                p.getLibMatFac2()
        );
    }

}