package com.spartaecommerce.cart.presentation.controller.dto.request;

import com.spartaecommerce.cart.domain.command.CartUpdateItemQuantityCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartUpdateItemQuantityRequest(
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {
    public CartUpdateItemQuantityCommand toCommand(Long userId, Long productId) {
        return new CartUpdateItemQuantityCommand(userId, productId, quantity);
    }
}