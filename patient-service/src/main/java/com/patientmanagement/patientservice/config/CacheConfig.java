package com.patientmanagement.patientservice.config; // Or your appropriate config package

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching // Ensures caching is turned on
public class CacheConfig {

    /**
     * This method defines the CacheManager bean. Spring Boot will now use this
     * configuration to manage all caches, including the one for user permissions.
     *
     * @param redisConnectionFactory Automatically provided by Spring Boot because of your
     *                               application.properties and Redis starter dependency.
     * @return A configured RedisCacheManager.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // This configuration sets a default Time-To-Live (TTL) for all cache entries.
        // Here, we're setting it to 30 minutes.
        RedisCacheConfiguration redisCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues(); // Optional: prevent caching of null values

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfig)
                .build();
    }
}