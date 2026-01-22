package com.spartaecommerce.user.application.dto.result;

import com.spartaecommerce.user.domain.entity.User;

import java.time.LocalDateTime;

public record UserResult(
    Long userId,
    String email,
    String name,
    String phoneNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResult from(User user) {
        return new UserResult(
            user.getUserId(),
            user.getEmail(),
            user.getName(),
            user.getPhoneNumber(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
