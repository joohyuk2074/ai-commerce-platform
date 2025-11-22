package com.spartaecommerce.user.presentation.controller.dto.request;

import com.spartaecommerce.user.application.dto.command.UserCreateCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Name is required")
    String name,

    String phoneNumber
) {
    public UserCreateCommand toCommand() {
        return new UserCreateCommand(email, name, phoneNumber);
    }
}
