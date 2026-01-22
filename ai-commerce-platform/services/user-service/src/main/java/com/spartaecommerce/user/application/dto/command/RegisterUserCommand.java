package com.spartaecommerce.user.application.dto.command;

public record RegisterUserCommand(
    String username,
    String password,
    String email,
    String name,
    String phoneNumber
) {
}
