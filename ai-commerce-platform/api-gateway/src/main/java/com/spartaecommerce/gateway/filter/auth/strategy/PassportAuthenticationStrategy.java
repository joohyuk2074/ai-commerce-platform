package com.spartaecommerce.gateway.filter.auth.strategy;

import com.spartaecommerce.gateway.jwt.JwtAuthenticationException;
import com.spartaecommerce.gateway.jwt.JwtTokenBlacklistService;
import com.spartaecommerce.gateway.jwt.JwtTokenProvider;
import com.spartaecommerce.gateway.passport.PassportCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Passport 인증 전략 (Toss 방식)
 *
 * Gateway에서 Auth Service로부터 Passport를 받아 Backend에 전달합니다.
 *
 * 인증 흐름:
 * 1. JWT 토큰 검증 (로컬)
 * 2. 블랙리스트 확인
 * 3. PassportCacheService를 통해 Passport 획득 (Redis Cache → Auth Service)
 * 4. Passport를 Exchange 속성에 저장 (Filter에서 X-Passport 헤더로 추가)
 * 5. AuthenticationResult 반환
 *
 * 장점:
 * - Backend 서비스가 Auth Service 호출 불필요 (Passport에 모든 정보 포함)
 * - 권한 변경 시 Passport 갱신으로 즉시 반영
 * - 일관된 사용자 정보 전달
 * - Redis 캐싱으로 Auth Service 부하 감소
 *
 * 단점:
 * - 최초 요청 시 Auth Service 호출 오버헤드 (캐시 미스)
 * - HTTP 헤더 크기 증가 (GZIP 압축으로 완화)
 */
@Slf4j
@Component("passportStrategy")
@RequiredArgsConstructor
public class PassportAuthenticationStrategy implements AuthenticationStrategy {

    public static final String PASSPORT_ATTRIBUTE_KEY = "passport";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenBlacklistService blacklistService;
    private final PassportCacheService passportCacheService;

    @Override
    public Mono<AuthenticationResult> authenticate(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
            .getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No Authorization header found or invalid format");
            return Mono.just(AuthenticationResult.unauthenticated());
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 1. 블랙리스트 확인 (로그아웃된 토큰)
        return blacklistService.isBlacklisted(token)
            .flatMap(isBlacklisted -> {
                if (isBlacklisted) {
                    log.warn("Token is blacklisted (logged out)");
                    return Mono.just(AuthenticationResult.unauthenticated());
                }

                try {
                    // 2. JWT 토큰 로컬 검증 (서명, 만료)
                    if (!jwtTokenProvider.validateToken(token)) {
                        log.warn("Invalid JWT token");
                        return Mono.just(AuthenticationResult.unauthenticated());
                    }

                    // 3. JWT에서 userId 추출
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);

                    // 4. PassportCacheService를 통해 Passport 획득
                    return passportCacheService.getOrCreatePassport(token, userId)
                        .flatMap(passport -> {
                            // 5. Passport를 Exchange 속성에 저장
                            exchange.getAttributes().put(PASSPORT_ATTRIBUTE_KEY, passport);

                            log.debug("Passport authentication successful for user: {} (ID: {}), passportId: {}",
                                passport.username(), passport.userId(), passport.passportId());

                            return Mono.just(AuthenticationResult.authenticated(
                                passport.userId(),
                                passport.roles()
                            ));
                        })
                        .onErrorResume(error -> {
                            log.error("Failed to get or create Passport for userId: {}", userId, error);
                            return Mono.just(AuthenticationResult.unauthenticated());
                        });

                } catch (JwtAuthenticationException e) {
                    log.warn("JWT authentication failed: {}", e.getMessage());
                    return Mono.just(AuthenticationResult.unauthenticated());
                }
            })
            .onErrorResume(error -> {
                log.error("Error during Passport authentication", error);
                return Mono.just(AuthenticationResult.unauthenticated());
            });
    }

    @Override
    public String getAuthenticationType() {
        return "passport";
    }
}
