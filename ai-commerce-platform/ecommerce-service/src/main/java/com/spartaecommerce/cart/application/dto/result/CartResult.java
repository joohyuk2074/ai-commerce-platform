package com.spartaecommerce.cart.application.dto.result;

import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.common.domain.Money;

import java.math.BigDecimal;
import java.util.List;

public record CartResult(
    Long userId,
    List<CartItemResult> items,
    Money totalAmount,
    BigDecimal expectedPoints
) {
    public static CartResult from(Cart cart, BigDecimal expectedPoints) {
        return new CartResult(
            cart.getUserId(),
            cart.getItems().stream()
                .map(CartItemResult::from)
                .toList(),
            cart.getTotalAmount(),
            expectedPoints
        );
    }
}