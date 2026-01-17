package com.spartaecommerce.gateway.filter.auth.strategy;

import java.util.ArrayList;
import java.util.List;

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
