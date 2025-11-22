package com.spartaecommerce.cart.domain.command;

public record CartRemoveItemCommand(
    Long userId,
    Long productId
) {
}