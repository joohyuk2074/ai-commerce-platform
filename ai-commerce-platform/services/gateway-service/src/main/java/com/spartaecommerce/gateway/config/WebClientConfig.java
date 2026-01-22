package com.spartaecommerce.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 설정
 * Backend 서비스(ecommerce-service, auth-service)와 통신하기 위한 WebClient 구성
 */
@Configuration
public class WebClientConfig {

    @Value("${services.ecommerce.url}")
    private String ecommerceServiceUrl;

    @Value("${services.auth.url}")
    private String authServiceUrl;

    /**
     * ecommerce-service와 통신을 위한 WebClient Bean
     */
    @Bean
    public WebClient ecommerceWebClient() {
        return WebClient.builder()
            .baseUrl(ecommerceServiceUrl)
            .build();
    }

    /**
     * auth-service와 통신을 위한 WebClient Bean
     * JWT 토큰 검증 등에 사용
     */
    @Bean
    public WebClient authServiceWebClient() {
        return WebClient.builder()
            .baseUrl(authServiceUrl)
            .build();
    }
}
