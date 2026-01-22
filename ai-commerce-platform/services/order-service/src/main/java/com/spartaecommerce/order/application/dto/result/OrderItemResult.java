package com.spartaecommerce.order.application.dto.result;

import com.spartaecommerce.order.domain.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResult(
    Long orderItemId,
    Long productId,
    String productName,
    BigDecimal price,
    Integer quantity,
    BigDecimal subtotal
) {
    public static OrderItemResult from(OrderItem item) {
        return new OrderItemResult(
            item.getOrderItemId(),
            item.getProductId(),
            item.getProductName(),
            item.getProductPrice().amount(),
            item.getQuantity(),
            item.getTotalPrice().amount()
        );
    }

}
