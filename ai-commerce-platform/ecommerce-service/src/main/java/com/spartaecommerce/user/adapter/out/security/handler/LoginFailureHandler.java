package com.spartaecommerce.user.adapter.out.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.common.domain.CommonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 실패 시 처리를 담당하는 핸들러
 * - 다양한 인증 예외에 따라 적절한 에러 메시지를 JSON 형식으로 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {

        log.error("Login failed: {}", exception.getMessage());

        String errorMessage = getErrorMessage(exception);

        CommonResponse<Void> errorResponse = CommonResponse.error("AUTH_ERROR", errorMessage);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * 예외 타입에 따라 적절한 에러 메시지 반환
     */
    private String getErrorMessage(AuthenticationException exception) {
        return switch (exception) {
            case BadCredentialsException ignored -> "아이디 또는 비밀번호가 올바르지 않습니다.";
            case UsernameNotFoundException ignored -> "존재하지 않는 사용자입니다.";
            case DisabledException ignored -> "비활성화된 계정입니다.";
            case InternalAuthenticationServiceException ignored ->
                "내부 인증 서비스 오류가 발생했습니다.";
            case null, default -> "인증에 실패했습니다.";
        };
    }
}
