package com.spartaecommerce.cart.presentation.controller.dto.response;

import com.spartaecommerce.cart.application.dto.result.CartResult;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
    Long userId,
    List<CartItemResponse> items,
    BigDecimal totalOriginalAmount,
    BigDecimal expectedPoints
) {
    public static CartResponse from(CartResult result) {
        return new CartResponse(
            result.userId(),
            result.items().stream()
                .map(CartItemResponse::from)
                .toList(),
            result.totalAmount().amount(),
            result.expectedPoints()
        );
    }
}