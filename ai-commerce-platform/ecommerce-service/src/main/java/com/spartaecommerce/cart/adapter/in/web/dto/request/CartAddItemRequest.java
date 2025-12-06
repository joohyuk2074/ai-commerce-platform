package com.spartaecommerce.cart.adapter.in.web.dto.request;

import com.spartaecommerce.cart.domain.command.CartAddItemCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartAddItemRequest(
    @NotNull(message = "Product ID is required")
    Long productId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {
    public CartAddItemCommand toCommand(Long userId) {
        return new CartAddItemCommand(userId, productId, quantity);
    }
}