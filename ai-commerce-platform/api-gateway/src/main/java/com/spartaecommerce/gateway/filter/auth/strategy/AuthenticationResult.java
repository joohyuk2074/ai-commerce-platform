package com.spartaecommerce.gateway.filter.auth.strategy;

import java.util.ArrayList;
import java.util.List;

/**
 * 인증 결과를 담는 DTO
 *
 * @param authenticated 인증 여부
 * @param userId        사용자 ID
 */
public record AuthenticationResult(
    boolean authenticated,
    Long userId,
    List<String> roles
) {

    public static AuthenticationResult unauthenticated() {
        return new AuthenticationResult(false, null, List.of());
    }

    public static AuthenticationResult authenticated(Long userId, List<String> roles) {
        return new AuthenticationResult(true, userId, new ArrayList<>(roles));
    }
}
