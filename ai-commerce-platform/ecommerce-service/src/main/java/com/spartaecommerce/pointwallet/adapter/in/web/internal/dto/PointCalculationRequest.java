package com.spartaecommerce.pointwallet.adapter.in.web.internal.dto;

import java.math.BigDecimal;
import java.util.List;

public record PointCalculationRequest(
    List<OrderItemDto> orderItems,
    String userGrade  // VIP, NORMAL, etc.
) {
    public record OrderItemDto(
        Long productId,
        Long categoryId,
        BigDecimal totalPrice
    ) {
    }
}
