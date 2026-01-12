package com.spartaecommerce.user.adapter.in.web.dto;

import com.spartaecommerce.user.application.dto.UserInfo;
import com.spartaecommerce.user.domain.entity.UserGrade;

public record UserInfoResponse(
    Long userId,
    String username,
    String email,
    String name,
    String phoneNumber,
    UserGrade grade
) {
    public static UserInfoResponse from(UserInfo userInfo) {
        return new UserInfoResponse(
            userInfo.userId(),
            userInfo.username(),
            userInfo.email(),
            userInfo.name(),
            userInfo.phoneNumber(),
            userInfo.grade()
        );
    }
}
