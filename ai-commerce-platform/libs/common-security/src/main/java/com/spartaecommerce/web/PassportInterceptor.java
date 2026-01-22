package com.spartaecommerce.web;

import com.spartaecommerce.context.PassportContext;
import com.spartaecommerce.domain.Passport;
import com.spartaecommerce.serialization.PassportSerializer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Passport 인터셉터
 *
 * Gateway로부터 전달받은 X-Passport 헤더를 파싱하여 PassportContext에 저장합니다.
 *
 * <p>동작 흐름:</p>
 * 1. X-Passport 헤더에서 직렬화된 Passport 추출
 * 2. PassportSerializer로 역직렬화
 * 3. PassportContext (ThreadLocal)에 저장
 * 4. 요청 처리 완료 후 Context 정리 (afterCompletion)
 *
 * <p>사용 예시 (Service Layer):</p>
 * <pre>
 * public void createOrder() {
 *     Passport passport = PassportContext.require();
 *     Long userId = passport.getUserId();
 *     String username = passport.getUsername();
 *     // ... business logic
 * }
 * </pre>
 */
@Slf4j
@Component
public class PassportInterceptor implements HandlerInterceptor {

    private static final String PASSPORT_HEADER = "X-Passport";

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {
        String passportHeader = request.getHeader(PASSPORT_HEADER);

        if (passportHeader == null || passportHeader.isBlank()) {
            log.debug("No X-Passport header found in request");
            return true;
        }

        try {
            // Passport 역직렬화
            Passport passport = PassportSerializer.deserialize(passportHeader);

            // 만료 확인
            if (passport.isExpired()) {
                log.warn("Passport is expired: passportId={}, expiresAt={}",
                    passport.passportId(), passport.expiresAt());
                return true;
            }

            // ThreadLocal에 저장
            PassportContext.set(passport);

            log.debug("Passport loaded for user: {} (ID: {}), passportId: {}",
                passport.username(), passport.userId(), passport.passportId());

        } catch (Exception e) {
            log.error("Failed to parse X-Passport header", e);
            // Passport 파싱 실패 시에도 요청은 계속 진행
            // (Gateway에서 이미 인증을 통과했으므로)
        }

        return true;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception ex
    ) {
        // ThreadLocal 정리 (메모리 누수 방지)
        PassportContext.clear();
        log.trace("Passport context cleared");
    }
}
