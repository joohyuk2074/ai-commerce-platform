package com.spartaecommerce.auth.application.service;

import com.spartaecommerce.auth.config.properties.JwtProperties;
import com.spartaecommerce.auth.domain.port.in.LoginUseCase;
import com.spartaecommerce.auth.domain.port.out.JwtTokenPort;
import com.spartaecommerce.auth.dto.LoginRequest;
import com.spartaecommerce.auth.dto.LoginResponse;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import com.spartaecommerce.user.domain.port.out.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService implements LoginUseCase {

    private final LoadUserPort loadUserPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final JwtTokenPort jwtTokenPort;
    private final JwtProperties jwtProperties;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = loadUserPort.findByUsername(request.username())
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "User not found: " + request.username()
            ));

        if (!passwordEncoderPort.matches(request.password(), user.getPassword())) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Invalid password"
            );
        }

        if (user.isDeleted()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Deleted user cannot login"
            );
        }

        String role = user.getGrade().name();
        String accessToken = jwtTokenPort.createAccessToken(
            user.getUserId(),
            user.getUsername(),
            role
        );
        String refreshToken = jwtTokenPort.createRefreshToken(user.getUserId());

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
}
