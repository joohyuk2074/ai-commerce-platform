package com.spartaecommerce.order.application.dto.result;

import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResult(
    Long orderId,
    Long userId,
    OrderStatus status,
    BigDecimal totalAmount,
    String shippingAddress,
    List<OrderItemResult> orderItems,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static OrderResult from(Order order) {
        List<OrderItemResult> orderItemResults = order.getOrderItems().stream()
            .map(OrderItemResult::from)
            .toList();

        return new OrderResult(
            order.getOrderId(),
            order.getUserId(),
            order.getStatus(),
            order.calculateTotalAmount(),
            order.getShippingAddress(),
            orderItemResults,
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
