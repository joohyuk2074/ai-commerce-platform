package com.spartaecommerce.gateway.filter.auth;

import com.spartaecommerce.domain.Passport;
import com.spartaecommerce.gateway.filter.auth.strategy.AuthenticationResult;
import com.spartaecommerce.gateway.filter.auth.strategy.AuthenticationStrategy;
import com.spartaecommerce.serialization.PassportSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.spartaecommerce.gateway.filter.auth.strategy.PassportAuthenticationStrategy.PASSPORT_ATTRIBUTE_KEY;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * 인증 및 권한 체크를 수행하는 Gateway Filter Factory
 * Route의 metadata에 정의된 access-level을 기반으로 권한을 체크
 */
@Slf4j
@Component
public class AuthenticationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final AuthenticationStrategy authenticationStrategy;

    public AuthenticationGatewayFilterFactory(AuthenticationStrategy authenticationStrategy) {
        super(Config.class);
        this.authenticationStrategy = authenticationStrategy;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Route metadata에서 access-level 추출
            Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
            if (route == null) {
                return chain.filter(exchange);
            }

            String accessLevelStr = (String) route.getMetadata().get("access-level");
            AccessLevel accessLevel = AccessLevel.fromString(accessLevelStr);

            if (accessLevel == AccessLevel.PUBLIC) {
                return chain.filter(exchange);
            }

            // 인증 수행
            return authenticationStrategy.authenticate(exchange)
                .flatMap(authResult -> {
                    if (!isAuthorized(exchange, accessLevel, authResult)) {
                        return unauthorized(exchange);
                    }

                    // 인증 정보를 헤더에 추가하여 downstream 서비스로 전달
                    ServerWebExchange modifiedExchange = addAuthenticationHeaders(exchange, authResult);
                    return chain.filter(modifiedExchange);
                })
                .onErrorResume(e -> {
                    // 인증 과정에서 오류 발생 시 401 반환
                    return unauthorized(exchange);
                });
        };
    }

    /**
     * 권한 체크
     */
    private boolean isAuthorized(
        ServerWebExchange exchange,
        AccessLevel accessLevel,
        AuthenticationResult authResult
    ) {
        return switch (accessLevel) {
            case PUBLIC -> true;

            case AUTHENTICATED, ADMIN -> authResult.authenticated();

            case PUBLIC_READ_ADMIN_WRITE -> {
                HttpMethod method = exchange.getRequest().getMethod();
                // GET 요청은 인증 불필요
                if (method == HttpMethod.GET) {
                    yield true;
                }

                yield authResult.authenticated();
            }

        };
    }

    private ServerWebExchange addAuthenticationHeaders(
        ServerWebExchange exchange,
        AuthenticationResult authResult
    ) {
        if (!authResult.authenticated()) {
            return exchange;
        }

        return exchange.mutate()
            .request(builder -> {
                if (authResult.userId() != null) {
                    builder.header("X-User-Id", String.valueOf(authResult.userId()));
                }

                // Passport 인증인 경우 X-Passport 헤더 추가
                Passport passport = exchange.getAttribute(PASSPORT_ATTRIBUTE_KEY);
                if (passport != null) {
                    try {
                        String serializedPassport = PassportSerializer.serialize(passport);
                        builder.header("X-Passport", serializedPassport);
                        log.debug("Added X-Passport header for user: {} (ID: {})",
                            passport.username(), passport.userId());
                    } catch (Exception e) {
                        log.error("Failed to serialize Passport for header", e);
                    }
                }
            })
            .build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // 필요시 설정 추가
    }
}
