package com.spartaecommerce.common.web.config;

import com.spartaecommerce.common.web.interceptor.AuthenticationInterceptor;
import com.spartaecommerce.common.web.resolver.AuthenticatedUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

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
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/**")  // /api로 시작하는 모든 경로에 적용
            .excludePathPatterns(EXCLUDE_PATHS);  // 제외 경로 설정
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedUserArgumentResolver);
    }
}
