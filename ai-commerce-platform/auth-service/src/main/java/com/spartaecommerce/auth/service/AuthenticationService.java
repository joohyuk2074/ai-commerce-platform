package com.spartaecommerce.auth.service;

import com.spartaecommerce.auth.config.properties.JwtProperties;
import com.spartaecommerce.auth.dto.LoginRequest;
import com.spartaecommerce.auth.dto.LoginResponse;
import com.spartaecommerce.auth.jwt.JwtTokenProvider;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import com.spartaecommerce.user.domain.port.out.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService {

    private final LoadUserPort loadUserPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    /**
     * 로그인 처리 및 JWT 토큰 발급
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 사용자 조회
        User user = loadUserPort.findByUsername(request.username())
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "User not found: " + request.username()
            ));

        // 2. 비밀번호 검증
        String encodedPassword = passwordEncoderPort.encode(request.password());
        if (!user.isPasswordMatch(request.password(), encodedPassword)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Invalid password"
            );
        }

        // 3. 탈퇴한 사용자 체크
        if (user.isDeleted()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Deleted user cannot login"
            );
        }

        // 4. JWT 토큰 생성
        String role = user.getGrade().name();
        String accessToken = jwtTokenProvider.createAccessToken(
            user.getUserId(),
            user.getUsername(),
            role
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        log.info("User {} logged in successfully", user.getUserId());

        return LoginResponse.of(
            accessToken,
            refreshToken,
            user.getUserId(),
            user.getUsername(),
            role,
            jwtProperties.getAccessTokenExpiration()
        );
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token 발급
     */
    public LoginResponse refreshAccessToken(String refreshToken) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Invalid refresh token"
            );
        }

        // 2. Refresh Token에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

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
        String newAccessToken = jwtTokenProvider.createAccessToken(
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
