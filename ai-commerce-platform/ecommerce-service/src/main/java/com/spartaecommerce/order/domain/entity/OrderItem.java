package com.spartaecommerce.order.domain.entity;

import com.spartaecommerce.common.domain.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderItem {

    private Long orderItemId;

    private Long productId;
    private String productName;
    private Money productPrice;
    private Integer quantity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderItem createNew(Long productId, String productName, Money price, Integer quantity) {
        return OrderItem.builder()
            .productId(productId)
            .productName(productName)
            .productPrice(price)
            .quantity(quantity)
            .build();
    }

    public Money getTotalPrice() {
        return productPrice.multiply(quantity);
    }
}
