package com.spartaecommerce.gateway.passport;

import com.spartaecommerce.common.auth.Passport;
import com.spartaecommerce.common.auth.PassportSerializer;
import com.spartaecommerce.common.domain.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Passport 캐싱 서비스
 *
 * Auth Service로부터 Passport를 받아 Redis에 캐싱합니다.
 * - Cache hit: Redis에서 직접 반환 (빠른 응답)
 * - Cache miss: Auth Service 호출 → Redis에 캐싱 → 반환
 *
 * 캐시 TTL: 55분 (Passport의 1시간 TTL보다 5분 짧게 설정하여 만료된 Passport 사용 방지)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PassportCacheService {

    private static final String CACHE_KEY_PREFIX = "passport:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(55);

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final WebClient authServiceWebClient;

    /**
     * JWT 토큰으로부터 Passport를 가져옵니다 (캐시 우선)
     *
     * @param jwtToken JWT Access Token (Bearer prefix 제거된 상태)
     * @param userId 사용자 ID (캐시 키로 사용)
     * @return Passport 객체
     */
    public Mono<Passport> getOrCreatePassport(String jwtToken, Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        return reactiveRedisTemplate.opsForValue()
            .get(cacheKey)
            .doOnNext(cached -> log.debug("Passport cache hit for userId: {}", userId))
            .map(PassportSerializer::deserialize)
            .filter(passport -> !passport.isExpired())
            .switchIfEmpty(
                fetchPassportFromAuthService(jwtToken)
                    .doOnNext(passport -> log.debug("Passport cache miss for userId: {}, fetched from Auth Service", userId))
                    .flatMap(passport -> cachePassport(cacheKey, passport)
                        .thenReturn(passport)
                    )
            )
            .doOnError(e -> log.error("Failed to get or create Passport for userId: {}", userId, e));
    }

    /**
     * Auth Service로부터 Passport 생성 요청
     *
     * @param jwtToken JWT Access Token
     * @return Passport 객체
     */
    private Mono<Passport> fetchPassportFromAuthService(String jwtToken) {
        return authServiceWebClient
            .post()
            .uri("/api/v1/auth/passport")
            .header("Authorization", "Bearer " + jwtToken)
            .retrieve()
            .bodyToMono(PassportResponse.class)
            .map(response -> response.data())
            .doOnError(e -> log.error("Failed to fetch Passport from Auth Service", e));
    }

    /**
     * Passport를 Redis에 캐싱
     *
     * @param cacheKey Redis 캐시 키
     * @param passport Passport 객체
     * @return void Mono
     */
    private Mono<Boolean> cachePassport(String cacheKey, Passport passport) {
        try {
            String serialized = PassportSerializer.serialize(passport);
            return reactiveRedisTemplate.opsForValue()
                .set(cacheKey, serialized, CACHE_TTL)
                .doOnSuccess(success -> {
                    if (success) {
                        log.debug("Passport cached for key: {}, TTL: {} minutes", cacheKey, CACHE_TTL.toMinutes());
                    } else {
                        log.warn("Failed to cache Passport for key: {}", cacheKey);
                    }
                });
        } catch (Exception e) {
            log.error("Failed to serialize Passport for caching", e);
            return Mono.just(false);
        }
    }

    /**
     * Passport 캐시 무효화
     *
     * @param userId 사용자 ID
     * @return void Mono
     */
    public Mono<Boolean> invalidatePassport(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        return reactiveRedisTemplate.delete(cacheKey)
            .map(count -> count > 0)
            .doOnSuccess(deleted -> {
                if (deleted) {
                    log.info("Passport cache invalidated for userId: {}", userId);
                }
            });
    }

    /**
     * Auth Service의 Passport 응답 DTO
     */
    private record PassportResponse(
        boolean success,
        Passport data,
        String message
    ) {}
}
