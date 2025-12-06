package com.spartaecommerce.common.web.interceptor;

import com.spartaecommerce.user.adapter.out.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 인증 정보를 처리하는 인터셉터
 * SecurityContext에서 Authentication 객체를 가져와 userId를 추출하고 X-User-Id 헤더에 설정합니다.
 *
 * <p>동작 흐름:</p>
 * 1. SecurityContext에서 Authentication 객체 조회
 * 2. CustomUserDetails에서 userId 추출
 * 3. X-User-Id 헤더에 userId 설정
 *
 * <p>향후 Spring Cloud Gateway 도입 시:</p>
 * - Gateway가 X-User-Id 헤더를 직접 설정하므로 이 인터셉터는 비활성화 가능
 * - Controller 코드는 변경 불필요
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
            && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

            Long userId = userDetails.getUserId();

            // X-User-Id 헤더에 userId 설정
            request.setAttribute(USER_ID_HEADER, userId);

            log.debug("Set userId to request attribute: {}", userId);
        }

        return true;
    }
}
