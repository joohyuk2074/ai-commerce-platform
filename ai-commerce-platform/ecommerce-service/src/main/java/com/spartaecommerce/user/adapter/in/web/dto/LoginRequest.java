package com.spartaecommerce.user.adapter.in.web.dto;

import com.spartaecommerce.user.application.dto.command.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }
}
