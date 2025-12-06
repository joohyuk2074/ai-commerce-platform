package com.spartaecommerce.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 설정
 * Backend 서비스(ecommerce-service)와 통신하기 위한 WebClient 구성
 */
@Configuration
public class WebClientConfig {

    @Value("${services.ecommerce.url}")
    private String ecommerceServiceUrl;

    /**
     * ecommerce-service와 통신을 위한 WebClient Bean
     * 세션 검증 등 backend 서비스 호출에 사용
     */
    @Bean
    public WebClient ecommerceWebClient() {
        return WebClient.builder()
            .baseUrl(ecommerceServiceUrl)
            .build();
    }
}
