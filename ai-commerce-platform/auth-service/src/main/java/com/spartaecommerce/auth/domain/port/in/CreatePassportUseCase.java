package com.spartaecommerce.auth.domain.port.in;

import com.spartaecommerce.common.auth.Passport;

/**
 * Passport 생성 Use Case
 */
public interface CreatePassportUseCase {

    /**
     * JWT 토큰으로부터 Passport 생성
     *
     * @param token JWT 토큰
     * @return Passport 객체
     */
    Passport createPassportFromToken(String token);
}
