package com.spartaecommerce.user.application.dto.command;

public record LoginCommand(
    String username,
    String password
) {
}
