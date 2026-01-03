package com.spartaecommerce.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 캐시 설정
 * Caffeine 캐시 매니저 구성 및 통계 기능 활성화
 */
@Configuration
public class CacheConfig {

    /**
     * Caffeine 캐시 매니저 설정
     * - 통계 수집 활성화 (recordStats)
     * - 캐시 히트율, 미스율 등의 메트릭 수집 가능
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // application.yml의 설정 + 통계 수집 활성화
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(3600, TimeUnit.SECONDS)  // 1시간
            .recordStats()  // 통계 수집 활성화
        );

        return cacheManager;
    }
}
