package com.spartaecommerce.user.adapter.in.web.dto;

import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Name is required")
    String name,

    String phoneNumber
) {
    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(username, password, email, name, phoneNumber);
    }
}
