package com.officedubac.project.module.convocations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final ReactiveStringRedisTemplate redis;
    private final ObjectMapper mapper;

    private static final Duration TTL = Duration.ofDays(30);

    public <T> Mono<T> get(String key, TypeReference<T> type) {

        return redis.opsForValue().get(key)
                .flatMap(json -> {
                    try {
                        return Mono.just(mapper.readValue(json, type));
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                });
    }

    public <T> Mono<T> set(String key, T value) {

        try {
            String json = mapper.writeValueAsString(value);

            return redis.opsForValue()
                    .set(key, json, TTL)
                    .thenReturn(value);

        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}