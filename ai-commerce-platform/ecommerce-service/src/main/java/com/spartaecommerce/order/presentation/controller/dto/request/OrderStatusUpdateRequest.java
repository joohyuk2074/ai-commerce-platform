package com.spartaecommerce.order.presentation.controller.dto.request;

import com.spartaecommerce.order.application.dto.command.OrderStatusUpdateCommand;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
    @NotNull
    OrderStatus orderStatus
) {
    public OrderStatusUpdateCommand toCommand(Long orderId) {
        return new OrderStatusUpdateCommand(orderId, orderStatus);
    }
}
