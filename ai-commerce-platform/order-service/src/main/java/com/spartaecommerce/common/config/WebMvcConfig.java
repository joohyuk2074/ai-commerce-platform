package com.spartaecommerce.common.web.config;

import com.spartaecommerce.common.web.interceptor.PassportInterceptor;
import com.spartaecommerce.common.web.resolver.PassportArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final PassportInterceptor passportInterceptor;
    private final PassportArgumentResolver passportArgumentResolver;

    /**
     * 인증 제외 경로 패턴
     * Spring Security의 SECURITY_EXCLUDE_PATHS와 동일하게 유지
     */
    private static final String[] EXCLUDE_PATHS = {
        "/public/**",
        "/api/swagger-ui/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/api/v3/api-docs/**",
        "/v3/api-docs/**",
        "/favicon.ico",
        "/actuator/**",
        "/swagger-resources/**",
        "/external/**",
        "/api/v1/auth/**"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Passport 인터셉터 (Gateway로부터 X-Passport 헤더 파싱)
        registry.addInterceptor(passportInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(EXCLUDE_PATHS);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(passportArgumentResolver);
    }
}
