package com.spartaecommerce.refund.adapter.in.web.dto.response;

import com.spartaecommerce.refund.application.dto.result.RefundResult;
import com.spartaecommerce.refund.domain.entity.RefundStatus;

import java.time.LocalDateTime;

public record RefundResponse(
    Long refundId,
    Long userId,
    Long orderId,
    String reason,
    RefundStatus status,
    LocalDateTime createdAt
) {
    public static RefundResponse from(RefundResult refundResult) {
        return new RefundResponse(
            refundResult.refundId(),
            refundResult.userId(),
            refundResult.orderId(),
            refundResult.reason(),
            refundResult.status(),
            refundResult.createdAt()
        );
    }
}
