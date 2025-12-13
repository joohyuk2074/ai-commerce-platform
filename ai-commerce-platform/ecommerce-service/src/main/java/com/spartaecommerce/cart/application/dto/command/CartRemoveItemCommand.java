package com.spartaecommerce.cart.application.dto.command;

public record CartRemoveItemCommand(
    Long userId,
    Long productId
) {
}