package com.spartaecommerce.user.adapter.out.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.common.domain.CommonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그아웃 성공 시 처리를 담당하는 핸들러
 * - Redis 세션 무효화 (Spring Session이 자동 처리)
 * - SecurityContext 정리 (Spring Security가 자동 처리)
 * - JSON 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onLogoutSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {

        String username = authentication != null ? authentication.getName() : "unknown";
        log.info("Logout successful for user: {}", username);

        // JSON 응답 생성
        CommonResponse<Void> commonResponse = CommonResponse.success(null);

        // 응답 설정
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(commonResponse));
    }
}
