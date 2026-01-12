package com.spartaecommerce.user.adapter.in.web.dto.request;

import com.spartaecommerce.user.application.dto.command.UserUpdateCommand;

public record UserUpdateRequest(
    String name,
    String phoneNumber
) {
    public UserUpdateCommand toCommand() {
        return new UserUpdateCommand(name, phoneNumber);
    }
}
