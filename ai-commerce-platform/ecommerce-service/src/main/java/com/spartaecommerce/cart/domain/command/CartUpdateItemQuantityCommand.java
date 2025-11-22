package com.spartaecommerce.cart.domain.command;

public record CartUpdateItemQuantityCommand(
    Long userId,
    Long productId,
    Integer quantity
) {
}