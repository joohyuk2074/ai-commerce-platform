package com.spartaecommerce.auth.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    UserInfo user
) {
    public static LoginResponse of(
        String accessToken,
        String refreshToken,
        Long userId,
        String username,
        String role,
        Long expiresIn
    ) {
        UserInfo userInfo = new UserInfo(userId, username, role);
        return new LoginResponse(accessToken, refreshToken, "Bearer", expiresIn, userInfo);
    }
}

record UserInfo(
    Long userId,
    String username,
    String role
) {
}
