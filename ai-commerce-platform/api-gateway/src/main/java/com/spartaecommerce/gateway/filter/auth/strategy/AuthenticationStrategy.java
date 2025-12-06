package com.spartaecommerce.gateway.filter.auth.strategy;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 인증 전략 인터페이스
 * 세션, JWT 등 다양한 인증 방식을 추상화
 */
public interface AuthenticationStrategy {

    /**
     * 요청에서 인증 정보를 추출하고 검증
     *
     * @param exchange ServerWebExchange
     * @return 인증 결과
     */
    Mono<AuthenticationResult> authenticate(ServerWebExchange exchange);

    /**
     * 이 전략이 지원하는 인증 방식 이름
     *
     * @return 인증 방식 이름 (session, jwt 등)
     */
    String getAuthenticationType();
}
