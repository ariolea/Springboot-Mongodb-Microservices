package com.tvmaze.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache en memoria de las busquedas de TVmaze, que aplica rate limiting
 * (HTTP 429), por lo que evitar busquedas repetidas es parte del contrato de uso.
 * Los shows individuales no se cachean aqui: su cache vive en MongoDB.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Cache de busquedas de shows por criterio. */
    public static final String SHOW_SEARCH_CACHE = "showSearch";

    @Bean
    public CacheManager cacheManager(TvMazeProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(SHOW_SEARCH_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(properties.cache().maximumSize())
                .expireAfterWrite(properties.cache().timeToLive()));
        return cacheManager;
    }
}
