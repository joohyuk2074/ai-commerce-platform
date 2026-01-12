package com.spartaecommerce.gateway.jwt;

/**
 * JWT 인증 과정에서 발생하는 예외
 */
public class JwtAuthenticationException extends RuntimeException {

    public JwtAuthenticationException(String message) {
        super(message);
    }

    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
