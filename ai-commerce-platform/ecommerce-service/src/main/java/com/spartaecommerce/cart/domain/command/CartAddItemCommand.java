package com.spartaecommerce.cart.domain.command;

public record CartAddItemCommand(
    Long userId,
    Long productId,
    Integer quantity
) {
}