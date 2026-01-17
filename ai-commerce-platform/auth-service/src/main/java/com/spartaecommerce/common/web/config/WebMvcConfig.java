package com.spartaecommerce.common.web.config;

import com.spartaecommerce.common.web.interceptor.PassportInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final PassportInterceptor passportInterceptor;

    /**
     * 인증 제외 경로 패턴
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
        "/api/v1/auth/signup",    // 회원가입
        "/api/v1/auth/login",     // 로그인
        "/api/v1/auth/refresh",   // 토큰 갱신
        "/api/v1/auth/validate",  // JWT 검증 (Gateway 전용)
        "/api/v1/auth/passport"   // Passport 생성 (Gateway 전용)
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Passport 인터셉터 (Gateway로부터 X-Passport 헤더 파싱)
        registry.addInterceptor(passportInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(EXCLUDE_PATHS);
    }
}
