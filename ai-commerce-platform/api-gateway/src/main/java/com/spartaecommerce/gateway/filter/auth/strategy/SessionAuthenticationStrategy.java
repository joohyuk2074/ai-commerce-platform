package com.spartaecommerce.gateway.filter.auth.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 세션 기반 인증 전략
 * JSESSIONID 쿠키를 확인하여 인증 상태를 검증
 * ecommerce-service에 세션 검증 API를 호출하여 실제 세션 유효성을 확인
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionAuthenticationStrategy implements AuthenticationStrategy {

    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    private static final String VALIDATION_ENDPOINT = "/internal/v1/auth/validate-session";

    private final WebClient ecommerceWebClient;

    @Override
    public Mono<AuthenticationResult> authenticate(ServerWebExchange exchange) {
        HttpCookie sessionCookie = exchange.getRequest()
            .getCookies()
            .getFirst(SESSION_COOKIE_NAME);

        if (sessionCookie == null || sessionCookie.getValue().isBlank()) {
            log.debug("세션 쿠키가 없습니다.");
            return Mono.just(AuthenticationResult.unauthenticated());
        }

        // ecommerce-service에 세션 검증 API 호출
        return validateSessionWithBackend(sessionCookie.getValue())
            .doOnSuccess(result -> {
                if (result.authenticated()) {
                    log.debug("세션 검증 성공: userId={}", result.userId());
                } else {
                    log.debug("세션 검증 실패");
                }
            })
            .doOnError(error -> log.error("세션 검증 중 오류 발생", error))
            .onErrorResume(error -> {
                // 세션 검증 API 호출 실패 시 인증되지 않은 것으로 처리
                log.warn("세션 검증 API 호출 실패, 인증 거부 처리: {}", error.getMessage());
                return Mono.just(AuthenticationResult.unauthenticated());
            });
    }

    /**
     * Backend 서비스(ecommerce-service)에 세션 검증 요청
     *
     * @param sessionId JSESSIONID 쿠키 값
     * @return 인증 결과
     */
    private Mono<AuthenticationResult> validateSessionWithBackend(String sessionId) {
        return ecommerceWebClient
            .get()
            .uri(VALIDATION_ENDPOINT)
            .cookie(SESSION_COOKIE_NAME, sessionId)
            .retrieve()
            .onStatus(
                status -> status.equals(HttpStatus.UNAUTHORIZED),
                response -> {
                    log.debug("세션이 유효하지 않음 (401 응답)");
                    return Mono.empty();
                }
            )
            .bodyToMono(SessionValidationResponse.class)
            .map(response -> {
                if (response != null) {
                    SessionValidationResponse.SessionData sessionData = response.data();
                    if (sessionData != null) {
                        Long userId = sessionData.userId();
                        return AuthenticationResult.authenticated(userId, sessionData.roles());
                    }
                }
                return AuthenticationResult.unauthenticated();
            })
            .defaultIfEmpty(AuthenticationResult.unauthenticated());
    }

    @Override
    public String getAuthenticationType() {
        return "session";
    }

    /**
     * 세션 검증 API 응답 DTO
     * ecommerce-service의 CommonResponse 구조에 맞춤
     */
    private record SessionValidationResponse(
        String status,
        SessionData data,
        String error
    ) {
        private record SessionData(
            Long userId,
            String username,
            List<String> roles
        ) {}
    }
}
