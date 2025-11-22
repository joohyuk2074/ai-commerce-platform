package com.spartaecommerce.cart.presentation.controller.dto.response;

import com.spartaecommerce.cart.application.dto.result.CartItemResult;

import java.math.BigDecimal;

public record CartItemResponse(
    Long productId,
    Integer quantity,
    BigDecimal originalPrice,
    BigDecimal totalPrice
) {
    public static CartItemResponse from(CartItemResult result) {
        return new CartItemResponse(
            result.productId(),
            result.quantity(),
            result.price().amount(),
            result.totalPrice().amount()
        );
    }
}