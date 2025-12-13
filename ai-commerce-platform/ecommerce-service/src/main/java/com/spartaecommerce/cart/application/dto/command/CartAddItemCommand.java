package com.spartaecommerce.cart.application.dto.command;

public record CartAddItemCommand(
    Long userId,
    Long productId,
    Integer quantity
) {
}