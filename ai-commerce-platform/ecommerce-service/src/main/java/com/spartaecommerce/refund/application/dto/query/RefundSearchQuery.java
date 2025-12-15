package com.spartaecommerce.refund.application.dto.query;

import com.spartaecommerce.common.domain.CustomPageable;
import com.spartaecommerce.refund.domain.entity.RefundStatus;

public record RefundSearchQuery(
    Long userId,
    RefundStatus refundStatus,
    CustomPageable pageable
) {
    public static RefundSearchQuery of(
        Long userId,
        RefundStatus refundStatus,
        Integer page,
        Integer size
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        CustomPageable customPageable = CustomPageable.of(page, size, null, null);
        return new RefundSearchQuery(userId, refundStatus, customPageable);
    }
}