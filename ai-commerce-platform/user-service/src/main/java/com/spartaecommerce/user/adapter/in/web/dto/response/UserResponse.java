package com.spartaecommerce.user.adapter.in.web.dto.response;

import com.spartaecommerce.user.application.dto.result.UserResult;

import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String email,
    String name,
    String phoneNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResponse from(UserResult user) {
        return new UserResponse(
            user.userId(),
            user.email(),
            user.name(),
            user.phoneNumber(),
            user.createdAt(),
            user.updatedAt()
        );
    }
}
