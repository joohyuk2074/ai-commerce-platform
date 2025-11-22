package com.spartaecommerce.refund.application.dto.command;

public record RefundCreateCommand(
    Long userId,
    Long orderId,
    String reason
) {
}