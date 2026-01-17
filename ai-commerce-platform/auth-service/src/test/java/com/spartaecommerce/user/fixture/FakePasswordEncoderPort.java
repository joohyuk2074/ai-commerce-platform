package com.spartaecommerce.user.fixture;

import com.spartaecommerce.user.domain.port.out.PasswordEncoderPort;

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
