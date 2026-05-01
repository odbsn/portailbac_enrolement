package com.officedubac.project.utils;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheClearRunner implements CommandLineRunner {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheClearRunner(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🧹 Nettoyage du cache Redis au démarrage...");
        redisTemplate.getConnectionFactory().getConnection().flushDb();
        // ou redisTemplate.getConnectionFactory().getConnection().flushAll(); pour tout vider
        System.out.println("✅ Cache Redis vidé avec succès !");
    }
}