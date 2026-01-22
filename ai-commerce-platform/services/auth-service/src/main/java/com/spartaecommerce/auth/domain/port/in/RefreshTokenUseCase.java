package com.spartaecommerce.auth.domain.port.in;

import com.spartaecommerce.auth.dto.LoginResponse;

/**
 * 토큰 갱신 Use Case
 */
public interface RefreshTokenUseCase {

    /**
     * Refresh Token을 사용하여 새로운 Access Token 발급
     *
     * @param refreshToken Refresh Token
     * @return 새로운 Access Token을 포함한 로그인 응답
     */
    LoginResponse refreshAccessToken(String refreshToken);
}
