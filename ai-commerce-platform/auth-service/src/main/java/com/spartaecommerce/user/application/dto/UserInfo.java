package com.spartaecommerce.user.application.dto;

import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.entity.UserGrade;

public record UserInfo(
    Long userId,
    String username,
    String email,
    String name,
    String phoneNumber,
    UserGrade grade
) {
    public static UserInfo from(User user) {
        return new UserInfo(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getName(),
            user.getPhoneNumber(),
            user.getGrade()
        );
    }
}
