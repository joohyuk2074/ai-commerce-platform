package com.spartaecommerce.user.application.dto.command;

public record UserUpdateCommand(
    String name,
    String phoneNumber
) {
}