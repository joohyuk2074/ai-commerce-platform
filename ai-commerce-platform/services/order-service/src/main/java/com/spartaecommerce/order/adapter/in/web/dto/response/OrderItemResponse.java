package com.spartaecommerce.order.adapter.in.web.dto.response;

import com.spartaecommerce.order.application.dto.result.OrderItemResult;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long orderItemId,
    Long productId,
    String productName,
    BigDecimal price,
    Integer quantity,
    BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItemResult itemInfo) {
        return new OrderItemResponse(
            itemInfo.orderItemId(),
            itemInfo.productId(),
            itemInfo.productName(),
            itemInfo.price(),
            itemInfo.quantity(),
            itemInfo.subtotal()
        );
    }
}
