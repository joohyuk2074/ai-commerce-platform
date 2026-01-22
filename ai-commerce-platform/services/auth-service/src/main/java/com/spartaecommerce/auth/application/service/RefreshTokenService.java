package com.spartaecommerce.auth.application.service;

import com.spartaecommerce.auth.config.properties.JwtProperties;
import com.spartaecommerce.auth.domain.port.in.RefreshTokenUseCase;
import com.spartaecommerce.auth.domain.port.out.JwtTokenPort;
import com.spartaecommerce.auth.dto.LoginResponse;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 토큰 갱신 Use Case 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService implements RefreshTokenUseCase {

    private final LoadUserPort loadUserPort;
    private final JwtTokenPort jwtTokenPort;
    private final JwtProperties jwtProperties;

    @Override
    public LoginResponse refreshAccessToken(String refreshToken) {
        // 1. Refresh Token 검증
        if (!jwtTokenPort.validateToken(refreshToken)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Invalid refresh token"
            );
        }

        // 2. Refresh Token에서 userId 추출
        Long userId = jwtTokenPort.getUserIdFromToken(refreshToken);

        // 3. 사용자 조회
        User user = loadUserPort.getById(userId);

        // 4. 탈퇴한 사용자 체크
        if (user.isDeleted()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Deleted user cannot refresh token"
            );
        }

        // 5. 새로운 Access Token 생성
        String role = user.getGrade().name();
        String newAccessToken = jwtTokenPort.createAccessToken(
            user.getUserId(),
            user.getUsername(),
            role
        );

        log.info("Access token refreshed for user {}", user.getUserId());

        return LoginResponse.of(
            newAccessToken,
            refreshToken, // 기존 Refresh Token 재사용
            user.getUserId(),
            user.getUsername(),
            role,
            jwtProperties.getAccessTokenExpiration()
        );
    }
}
