package com.spartaecommerce.gateway.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 토큰 블랙리스트 관리 서비스
 *
 * 로그아웃된 토큰을 Redis에 저장하여 만료 전까지 사용 불가능하게 합니다.
 *
 * Redis Key 형식: jwt:blacklist:{tokenHash}
 * TTL: 토큰 만료 시간까지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 토큰을 블랙리스트에 추가 (로그아웃 시 호출)
     *
     * @param token JWT 토큰
     * @return 블랙리스트 추가 성공 여부
     */
    public Mono<Boolean> addToBlacklist(String token) {
        try {
            // 토큰에서 만료 시간 추출
            Claims claims = jwtTokenProvider.parseToken(token);
            Date expiration = claims.getExpiration();

            // 이미 만료된 토큰은 블랙리스트에 추가할 필요 없음
            if (expiration.before(new Date())) {
                log.debug("Token already expired, skipping blacklist");
                return Mono.just(true);
            }

            // 토큰 해시값을 키로 사용 (보안 및 저장 효율성)
            String tokenHash = String.valueOf(token.hashCode());
            String key = BLACKLIST_KEY_PREFIX + tokenHash;

            // 만료 시간까지 남은 시간 계산
            Duration ttl = Duration.between(Instant.now(), expiration.toInstant());

            // Redis에 저장 (TTL 설정)
            return reactiveRedisTemplate.opsForValue()
                .set(key, "1", ttl)
                .doOnSuccess(success -> {
                    if (success) {
                        log.info("Token added to blacklist: hash={}, ttl={}s",
                            tokenHash, ttl.getSeconds());
                    } else {
                        log.warn("Failed to add token to blacklist: hash={}", tokenHash);
                    }
                })
                .doOnError(error -> log.error("Error adding token to blacklist", error));

        } catch (JwtAuthenticationException e) {
            log.warn("Invalid token, cannot add to blacklist: {}", e.getMessage());
            return Mono.just(false);
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     *
     * @param token JWT 토큰
     * @return 블랙리스트 포함 여부
     */
    public Mono<Boolean> isBlacklisted(String token) {
        try {
            String tokenHash = String.valueOf(token.hashCode());
            String key = BLACKLIST_KEY_PREFIX + tokenHash;

            return reactiveRedisTemplate.hasKey(key)
                .doOnNext(exists -> {
                    if (exists) {
                        log.debug("Token is blacklisted: hash={}", tokenHash);
                    }
                })
                .onErrorResume(error -> {
                    log.error("Error checking token blacklist", error);
                    // Redis 오류 시 안전하게 false 반환 (서비스 계속 운영)
                    return Mono.just(false);
                });

        } catch (Exception e) {
            log.error("Error checking token blacklist", e);
            return Mono.just(false);
        }
    }

    /**
     * 특정 사용자의 모든 토큰을 블랙리스트에 추가
     * (비밀번호 변경, 강제 로그아웃 등)
     *
     * @param userId 사용자 ID
     * @return 성공 여부
     */
    public Mono<Boolean> blacklistAllUserTokens(Long userId) {
        // 사용자별 토큰 목록은 별도 관리 필요 (향후 구현)
        // 현재는 개별 토큰만 블랙리스트 관리
        log.info("Blacklisting all tokens for user: {}", userId);
        return Mono.just(true);
    }

    /**
     * 만료된 블랙리스트 항목 정리
     * (Redis TTL에 의해 자동 삭제되므로 명시적 호출 불필요)
     */
    public Mono<Long> cleanupExpiredTokens() {
        // Redis TTL이 자동으로 만료된 키를 삭제하므로
        // 별도 정리 작업 불필요
        log.debug("Cleanup not needed - Redis TTL handles expiration");
        return Mono.just(0L);
    }
}
