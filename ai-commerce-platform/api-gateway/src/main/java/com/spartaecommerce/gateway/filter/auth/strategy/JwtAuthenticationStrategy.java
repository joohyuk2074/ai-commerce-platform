package com.spartaecommerce.gateway.filter.auth.strategy;

import com.spartaecommerce.gateway.jwt.JwtAuthenticationException;
import com.spartaecommerce.gateway.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT 기반 인증 전략
 * Authorization: Bearer {token} 헤더에서 JWT를 추출하여 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.strategy", havingValue = "jwt", matchIfMissing = false)
public class JwtAuthenticationStrategy implements AuthenticationStrategy {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

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

        try {
            // 1. JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Invalid JWT token");
                return Mono.just(AuthenticationResult.unauthenticated());
            }

            // 2. Claims에서 정보 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            log.debug("JWT authentication successful for user: {} (ID: {})", username, userId);

            // 3. AuthenticationResult 반환
            return Mono.just(AuthenticationResult.authenticated(userId, List.of(role)));

        } catch (JwtAuthenticationException e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            return Mono.just(AuthenticationResult.unauthenticated());
        }
    }

    @Override
    public String getAuthenticationType() {
        return "jwt";
    }
}
