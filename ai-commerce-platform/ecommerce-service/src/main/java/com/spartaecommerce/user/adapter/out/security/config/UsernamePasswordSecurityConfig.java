package com.spartaecommerce.user.adapter.out.security.config;

import com.spartaecommerce.user.adapter.out.security.handler.LoginFailureHandler;
import com.spartaecommerce.user.adapter.out.security.handler.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class UsernamePasswordSecurityConfig {

    public static final String[] SECURITY_EXCLUDE_PATHS = {
        "/public/**", "/api/swagger-ui/**", "/swagger-ui/**", "/swagger-ui.html",
        "/api/v3/api-docs/**", "/v3/api-docs/**", "/favicon.ico", "/actuator/**",
        "/swagger-resources/**", "/external/**", "/api/v1/auth/**"
    };

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final com.spartaecommerce.user.adapter.out.security.handler.LogoutSuccessHandler logoutSuccessHandler;

    @Bean
    SecurityFilterChain usernamePasswordAuthenticationConfig(HttpSecurity http) throws Exception {
        log.info("Init configure id/pw authentication.");

        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(SECURITY_EXCLUDE_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/api/v1/auth/login")
                .loginProcessingUrl("/api/v1/auth/login")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)  // 세션 무효화
                .deleteCookies("JSESSIONID")  // 쿠키 삭제
                .clearAuthentication(true)    // SecurityContext 인증 정보 삭제
                .permitAll()
            )
//            .exceptionHandling(ex -> ex
//                .authenticationEntryPoint((request, response, authException) -> {
//                    // 로그인 안 된 상태로 보호 자원 접근 시 401 JSON 리턴
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.setContentType("application/json;charset=UTF-8");
//                    response.getWriter().write(
//                        objectMapper.writeValueAsString(
//                            CommonResponse.failure("UNAUTHORIZED", "로그인이 필요합니다.")
//                        )
//                    );
//                })
//            )
        ;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
