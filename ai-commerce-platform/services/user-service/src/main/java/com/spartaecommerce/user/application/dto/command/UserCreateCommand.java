package com.spartaecommerce.user.application.dto.command;

public record UserCreateCommand(
    String email,
    String name,
    String phoneNumber
) {
}