package com.spartaecommerce.order.adapter.in.web.dto.request;

import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderItemCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreateRequest(
    @NotNull
    Long userId,

    @NotNull
    List<OrderItemCreateRequest> orderItemCreateRequests,

    @NotBlank
    String shippingAddress,

    BigDecimal usePointAmount
) {
    public OrderCreateRequest {
        if (orderItemCreateRequests == null) {
            orderItemCreateRequests = List.of();
        }
    }

    public OrderCreateCommand toCommand() {
        List<OrderItemCreateCommand> orderItemCreateCommands = orderItemCreateRequests.stream()
            .map(OrderItemCreateRequest::toCommand)
            .toList();

        return new OrderCreateCommand(
            userId,
            orderItemCreateCommands,
            shippingAddress,
            usePointAmount != null ? usePointAmount : BigDecimal.ZERO
        );
    }

    public record OrderItemCreateRequest(
        @NotNull
        @Positive
        Long productId,

        @Positive
        @NotNull
        Integer quantity
    ) {
        public OrderItemCreateCommand toCommand() {
            return new OrderItemCreateCommand(productId, quantity);
        }
    }
}
