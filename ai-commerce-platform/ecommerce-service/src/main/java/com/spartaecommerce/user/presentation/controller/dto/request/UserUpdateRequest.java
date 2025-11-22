package com.spartaecommerce.user.presentation.controller.dto.request;

import com.spartaecommerce.user.application.dto.command.UserUpdateCommand;

public record UserUpdateRequest(
    String name,
    String phoneNumber
) {
    public UserUpdateCommand toCommand() {
        return new UserUpdateCommand(name, phoneNumber);
    }
}
