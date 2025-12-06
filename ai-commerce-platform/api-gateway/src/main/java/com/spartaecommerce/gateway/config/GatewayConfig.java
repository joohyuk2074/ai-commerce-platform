package com.spartaecommerce.gateway.config;

import com.spartaecommerce.gateway.filter.auth.AuthenticationGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 라우팅 설정
 * 모든 route에 인증 필터를 자동으로 적용
 */
@Configuration
public class GatewayConfig {

    private final AuthenticationGatewayFilterFactory authenticationFilter;

    public GatewayConfig(AuthenticationGatewayFilterFactory authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    /**
     * 글로벌 필터로 인증 필터를 적용하는 대신,
     * 각 route에 명시적으로 적용할 수도 있습니다.
     *
     * 현재는 application.yml에서 route를 정의하고 있으므로,
     * default-filters를 통해 모든 route에 인증 필터가 자동 적용됩니다.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // application.yml에 정의된 route들이 기본적으로 사용됨
            // 필요시 여기서 추가 route 정의 가능
            .build();
    }
}
