package com.spartaecommerce.auth.application.service;

import com.spartaecommerce.auth.domain.port.in.ValidateTokenUseCase;
import com.spartaecommerce.auth.domain.port.out.JwtTokenPort;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.user.adapter.in.web.AuthController.TokenValidationResponse;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JWT 토큰 검증 Use Case 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ValidateTokenService implements ValidateTokenUseCase {

    private final LoadUserPort loadUserPort;
    private final JwtTokenPort jwtTokenPort;

    @Override
    public TokenValidationResponse validateToken(String authorizationHeader) {
        try {
            // 1. Authorization 헤더 검증
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return TokenValidationResponse.invalid("Missing or invalid Authorization header");
            }

            // 2. 토큰 추출
            String token = authorizationHeader.substring(7);

            // 3. 토큰 서명 및 만료 검증
            if (!jwtTokenPort.validateToken(token)) {
                return TokenValidationResponse.invalid("Invalid or expired token");
            }

            // 4. 토큰에서 사용자 정보 추출
            Long userId = jwtTokenPort.getUserIdFromToken(token);
            String username = jwtTokenPort.getUsernameFromToken(token);
            String role = jwtTokenPort.getRoleFromToken(token);

            // 5. 사용자 조회 (실시간 권한 확인)
            User user = loadUserPort.getById(userId);

            // 6. 탈퇴한 사용자 체크
            if (user.isDeleted()) {
                return TokenValidationResponse.invalid("User account is deleted");
            }

            // 7. 실시간 권한 확인 (권한이 변경되었을 수 있음)
            String currentRole = user.getGrade().name();
            List<String> roles = List.of(currentRole);

            log.debug("Token validated successfully for user: {} (ID: {})", username, userId);

            return TokenValidationResponse.valid(userId, username, roles);

        } catch (BusinessException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return TokenValidationResponse.invalid(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token validation", e);
            return TokenValidationResponse.invalid("Token validation error");
        }
    }
}
