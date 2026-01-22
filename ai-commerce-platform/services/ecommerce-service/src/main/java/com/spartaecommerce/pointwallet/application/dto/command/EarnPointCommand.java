package com.spartaecommerce.pointwallet.application.dto.command;

public record EarnPointCommand(
    Long userId,
    Money amount,
    String description
) {
}