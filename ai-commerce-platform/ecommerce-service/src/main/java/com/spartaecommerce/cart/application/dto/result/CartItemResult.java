package com.spartaecommerce.cart.application.dto.result;

import com.spartaecommerce.cart.domain.entity.CartItem;
import com.spartaecommerce.common.domain.Money;

public record CartItemResult(
    Long productId,
    Integer quantity,
    Money price,
    Money totalPrice
) {
    public static CartItemResult from(CartItem cartItem) {
        return new CartItemResult(
            cartItem.getProductId(),
            cartItem.getQuantity(),
            cartItem.getPrice(),
            cartItem.getTotalPrice()
        );
    }
}