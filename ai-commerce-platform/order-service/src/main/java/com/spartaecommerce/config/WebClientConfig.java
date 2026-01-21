package com.spartaecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${external.ecommerce-service.url}")
    private String ecommerceServiceUrl;

    @Bean
    public WebClient ecommerceServiceWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl(ecommerceServiceUrl)
            .build();
    }
}
