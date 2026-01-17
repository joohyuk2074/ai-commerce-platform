package com.spartaecommerce.auth.domain.port.in;

import com.spartaecommerce.user.adapter.in.web.AuthController.TokenValidationResponse;

/**
 * JWT 토큰 검증 Use Case
 */
public interface ValidateTokenUseCase {

    /**
     * JWT 토큰 검증 (API Gateway에서 호출)
     *
     * @param authorizationHeader Authorization 헤더 값 (Bearer {token})
     * @return 토큰 검증 결과
     */
    TokenValidationResponse validateToken(String authorizationHeader);
}
