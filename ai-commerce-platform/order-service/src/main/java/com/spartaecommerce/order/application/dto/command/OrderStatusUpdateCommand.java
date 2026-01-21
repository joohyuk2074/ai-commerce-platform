package com.spartaecommerce.order.application.dto.command;

import com.spartaecommerce.order.domain.entity.OrderStatus;

public record OrderStatusUpdateCommand(
    Long orderId,
    OrderStatus orderStatus
) {
}