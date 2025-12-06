package com.spartaecommerce.gateway.config;

import com.spartaecommerce.gateway.filter.auth.strategy.AuthenticationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 인증 관련 설정
 * application.yml의 auth.strategy 설정에 따라 인증 방식을 선택
 */
@Configuration
public class AuthenticationConfig {

    @Value("${auth.strategy:session}")
    private String authStrategy;

    /**
     * 사용할 인증 전략을 선택하여 Bean으로 등록
     *
     * @param strategies 등록된 모든 AuthenticationStrategy 구현체
     * @return 선택된 인증 전략
     */
    @Bean
    public AuthenticationStrategy authenticationStrategy(List<AuthenticationStrategy> strategies) {
        Map<String, AuthenticationStrategy> strategyMap = strategies.stream()
            .collect(Collectors.toMap(
                AuthenticationStrategy::getAuthenticationType,
                Function.identity()
            ));

        AuthenticationStrategy selectedStrategy = strategyMap.get(authStrategy);

        if (selectedStrategy == null) {
            throw new IllegalStateException(
                String.format("Unknown authentication strategy: %s. Available strategies: %s",
                    authStrategy, String.join(", ", strategyMap.keySet()))
            );
        }

        return selectedStrategy;
    }
}
