package com.spartaecommerce.order.application.dto.command;

public record OrderItemCreateCommand(
    Long productId,
    Integer quantity
) {
}
