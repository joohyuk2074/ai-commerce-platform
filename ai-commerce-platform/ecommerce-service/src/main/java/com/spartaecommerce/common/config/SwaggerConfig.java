package com.spartaecommerce.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String passportAuth = "Passport Authentication";

        return new OpenAPI()
            .info(new Info()
                .title("E-Commerce Platform API")
                .description("이커머스 플랫폼 API 문서")
                .version("v1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList(passportAuth))
            .components(new Components()
                .addSecuritySchemes(passportAuth, new SecurityScheme()
                    .name(passportAuth)
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("X-Passport")
                    .description("Gateway에서 발급한 Passport 토큰 (서버 간 인증)")));
    }
}
