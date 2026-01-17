package com.spartaecommerce.user.fixture;

import com.spartaecommerce.user.domain.port.out.PasswordEncoderPort;

/**
 * 테스트용 PasswordEncoderPort Fake 구현체
 * 간단한 접두사 기반 인코딩을 제공합니다.
 */
public class FakePasswordEncoderPort implements PasswordEncoderPort {

    @Override
    public String encode(String rawPassword) {
        return "encoded_" + rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encodedPassword.equals("encoded_" + rawPassword);
    }
}
