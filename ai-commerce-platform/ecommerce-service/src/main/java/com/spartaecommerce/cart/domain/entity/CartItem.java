package com.spartaecommerce.cart.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CartItem {

    private Long cartItemId;

    private Long cartId;

    private Long productId;

    private Integer quantity;

    private Money price;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static CartItem createNew(
        Long cartId,
        Long productId,
        Integer quantity,
        Money price
    ) {
        validateQuantity(quantity);
        validatePrice(price);

        return CartItem.builder()
            .cartId(cartId)
            .productId(productId)
            .quantity(quantity)
            .price(price)
            .build();
    }

    public void increaseQuantity(Integer additionalQuantity) {
        validateQuantity(additionalQuantity);
        this.quantity += additionalQuantity;
    }

    public void updateQuantity(Integer newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    public Money getTotalPrice() {
        return price.multiply(quantity);
    }

    private static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Quantity must be greater than 0"
            );
        }
    }

    private static void validatePrice(Money price) {
        if (price == null || price.isZero()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Price must be greater than 0"
            );
        }
    }
}