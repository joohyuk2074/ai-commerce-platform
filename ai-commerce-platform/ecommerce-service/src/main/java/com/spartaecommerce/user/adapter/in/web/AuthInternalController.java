package com.spartaecommerce.user.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.domain.port.in.GetUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/auth")
@RequiredArgsConstructor
public class AuthInternalController {

    private final GetUserUseCase getUserUseCase;

    /**
     * 세션 검증 API - API Gateway에서 세션 유효성 확인을 위해 사용
     * @param authentication Spring Security 인증 정보
     * @return 인증된 사용자의 ID
     */
    @GetMapping("/validate-session")
    public ResponseEntity<CommonResponse<SessionValidationResponse>> validateSession(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(CommonResponse.error("UNAUTHORIZED", "인증되지 않은 요청입니다."));
        }

        String username = authentication.getName();
        UserResult userResult = getUserUseCase.getByUsername(username);
        SessionValidationResponse response = new SessionValidationResponse(
            userResult.userId(),
            username,
            List.of("USER")
        );

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 세션 검증 응답 DTO
     */
    public record SessionValidationResponse(
        Long userId,
        String username,
        List<String> roles
    ) {}
}
