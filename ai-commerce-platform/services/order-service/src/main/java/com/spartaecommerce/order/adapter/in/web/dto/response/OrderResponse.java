package com.spartaecommerce.order.adapter.in.web.dto.response;

import com.spartaecommerce.order.application.dto.result.OrderResult;
import com.spartaecommerce.order.domain.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long orderId,
    Long userId,
    OrderStatus status,
    BigDecimal totalAmount,
    String shippingAddress,
    List<OrderItemResponse> orderItems,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static OrderResponse from(OrderResult orderResult) {
        List<OrderItemResponse> items = orderResult.orderItems().stream()
            .map(OrderItemResponse::from)
            .toList();

        return new OrderResponse(
            orderResult.orderId(),
            orderResult.userId(),
            orderResult.status(),
            orderResult.totalAmount(),
            orderResult.shippingAddress(),
            items,
            orderResult.createdAt(),
            orderResult.updatedAt()
        );
    }

}
