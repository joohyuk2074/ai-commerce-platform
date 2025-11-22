package com.spartaecommerce.refund.presentation.controller.dto.request;

import com.spartaecommerce.refund.domain.entity.RefundStatus;
import com.spartaecommerce.refund.application.dto.query.RefundSearchQuery;
import jakarta.validation.constraints.NotNull;

public record RefundSearchRequest(
    @NotNull
    Long userId,
    RefundStatus refundStatus,
    Integer page,
    Integer size,
    String sortedBy,
    String direction
) {
    public RefundSearchQuery toQuery() {
        return RefundSearchQuery.of(userId, refundStatus, page, size, sortedBy, direction);
    }
}
