package com.spartaecommerce.category.adapter.in.web;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.spartaecommerce.category.domain.port.out.CategoryClosurePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카테고리 관리자 API
 * 운영/관리 목적의 엔드포인트
 */
@Slf4j
@Tag(name = "Category Admin", description = "카테고리 관리 API (관리자용)")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryClosurePort categoryClosurePort;
    private final CacheManager cacheManager;

    @Operation(
        summary = "Closure Table 재구성",
        description = "기존 카테고리 데이터를 기반으로 Closure Table을 재구성합니다. " +
                     "데이터 마이그레이션이나 불일치 발생 시 사용합니다. 관리자 권한이 필요합니다."
    )
    @PostMapping("/closure-table/rebuild")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "categoryTree", allEntries = true)
    public ResponseEntity<RebuildResponse> rebuildClosureTable() {
        log.info("Starting manual closure table rebuild...");

        try {
            categoryClosurePort.rebuildClosureTable();
            log.info("Manual closure table rebuild completed successfully.");

            return ResponseEntity.ok(new RebuildResponse(
                true,
                "Closure table rebuilt successfully."
            ));
        } catch (Exception e) {
            log.error("Failed to rebuild closure table", e);
            return ResponseEntity.internalServerError()
                .body(new RebuildResponse(
                    false,
                    "Failed to rebuild closure table: " + e.getMessage()
                ));
        }
    }

    @Operation(
        summary = "카테고리 트리 캐시 통계 조회",
        description = "캐시 히트율, 미스율 등 캐시 성능 메트릭을 조회합니다. 관리자 권한이 필요합니다."
    )
    @GetMapping("/cache/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CacheStatsResponse> getCacheStats() {
        org.springframework.cache.Cache cache = cacheManager.getCache("categoryTree");

        if (cache == null) {
            return ResponseEntity.ok(new CacheStatsResponse(
                false,
                "Cache not found",
                null
            ));
        }

        if (cache instanceof CaffeineCache caffeineCache) {
            Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
            CacheStats stats = nativeCache.stats();

            double hitRate = stats.hitRate() * 100;
            double missRate = stats.missRate() * 100;

            log.info("Cache stats - Hit rate: {:.2f}%, Miss rate: {:.2f}%, " +
                    "Hits: {}, Misses: {}, Evictions: {}, Load success: {}, Load failure: {}",
                hitRate, missRate,
                stats.hitCount(), stats.missCount(), stats.evictionCount(),
                stats.loadSuccessCount(), stats.loadFailureCount());

            return ResponseEntity.ok(new CacheStatsResponse(
                true,
                "Cache stats retrieved successfully",
                new CacheMetrics(
                    stats.requestCount(),
                    stats.hitCount(),
                    stats.missCount(),
                    hitRate,
                    missRate,
                    stats.evictionCount(),
                    stats.loadSuccessCount(),
                    stats.loadFailureCount(),
                    stats.averageLoadPenalty() / 1_000_000.0  // nanoseconds to milliseconds
                )
            ));
        }

        return ResponseEntity.ok(new CacheStatsResponse(
            false,
            "Cache statistics not available (not a Caffeine cache)",
            null
        ));
    }

    @Operation(
        summary = "카테고리 트리 캐시 초기화",
        description = "카테고리 트리 캐시를 강제로 초기화합니다. 관리자 권한이 필요합니다."
    )
    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "categoryTree", allEntries = true)
    public ResponseEntity<RebuildResponse> clearCache() {
        log.info("Manual cache clear requested.");
        return ResponseEntity.ok(new RebuildResponse(
            true,
            "Cache cleared successfully."
        ));
    }

    public record RebuildResponse(
        boolean success,
        String message
    ) {}

    public record CacheStatsResponse(
        boolean success,
        String message,
        CacheMetrics metrics
    ) {}

    public record CacheMetrics(
        long totalRequests,
        long hitCount,
        long missCount,
        double hitRate,
        double missRate,
        long evictionCount,
        long loadSuccessCount,
        long loadFailureCount,
        double averageLoadTimeMs
    ) {}
}
