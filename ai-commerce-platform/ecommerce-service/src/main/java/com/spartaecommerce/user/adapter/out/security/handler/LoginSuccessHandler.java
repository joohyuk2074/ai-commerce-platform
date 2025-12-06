package com.spartaecommerce.user.adapter.out.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.common.domain.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 성공 시 처리를 담당하는 핸들러
 * - SecurityContext에 인증 정보 저장 (AbstractAuthenticationProcessingFilter가 자동 처리)
 * - 사용자 정보 조회 및 JSON 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        String name = authentication.getName();

        log.info("Login successful for user: {}", name);

        CommonResponse<String> loginSuccessResponse = CommonResponse.success(name);

        // 응답 설정
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(loginSuccessResponse));
    }
}
