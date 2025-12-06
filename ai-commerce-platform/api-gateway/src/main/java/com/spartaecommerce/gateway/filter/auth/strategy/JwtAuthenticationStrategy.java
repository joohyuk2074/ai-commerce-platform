package com.spartaecommerce.gateway.filter.auth.strategy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * JWT 기반 인증 전략 (향후 확장용)
 * Authorization: Bearer {token} 헤더에서 JWT를 추출하여 검증
 */
@Component
@ConditionalOnProperty(name = "auth.strategy", havingValue = "jwt", matchIfMissing = false)
public class JwtAuthenticationStrategy implements AuthenticationStrategy {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<AuthenticationResult> authenticate(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
            .getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Mono.just(AuthenticationResult.unauthenticated());
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // TODO: JWT 검증 로직 구현
        // 1. JWT 서명 검증
        // 2. 만료 시간 확인
        // 3. Claims에서 userId, username, roles 추출
        // 4. AuthenticationResult 반환

        // 현재는 스켈레톤 구현
        return Mono.just(AuthenticationResult.unauthenticated());
    }

    @Override
    public String getAuthenticationType() {
        return "jwt";
    }

    // TODO: JWT 파싱 및 검증 메서드 추가
    // private Claims parseToken(String token) { ... }
    // private boolean validateToken(String token) { ... }
    // private AuthenticationResult extractAuthenticationFromClaims(Claims claims) { ... }
}
