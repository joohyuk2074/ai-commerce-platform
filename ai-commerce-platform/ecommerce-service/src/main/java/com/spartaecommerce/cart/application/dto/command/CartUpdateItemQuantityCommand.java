package com.spartaecommerce.cart.application.dto.command;

public record CartUpdateItemQuantityCommand(
    Long userId,
    Long productId,
    Integer quantity
) {
}