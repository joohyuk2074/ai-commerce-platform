package com.spartaecommerce.auth.adapter.out.jwt;

import com.spartaecommerce.auth.domain.port.out.JwtTokenPort;
import com.spartaecommerce.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증을 위한 Outbound Adapter
 */
@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements JwtTokenPort {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String createAccessToken(Long userId, String username, String role) {
        return jwtTokenProvider.createAccessToken(userId, username, role);
    }

    @Override
    public String createRefreshToken(Long userId) {
        return jwtTokenProvider.createRefreshToken(userId);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    @Override
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    @Override
    public String getRoleFromToken(String token) {
        return jwtTokenProvider.getRoleFromToken(token);
    }
}
