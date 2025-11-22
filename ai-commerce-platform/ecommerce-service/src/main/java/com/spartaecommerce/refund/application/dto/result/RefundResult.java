package com.spartaecommerce.refund.application.dto.result;

import com.spartaecommerce.refund.domain.entity.Refund;
import com.spartaecommerce.refund.domain.entity.RefundStatus;

import java.time.LocalDateTime;

public record RefundResult(
    Long refundId,
    Long userId,
    Long orderId,
    String reason,
    RefundStatus status,
    LocalDateTime createdAt
) {
    public static RefundResult from(Refund refund) {
        return new RefundResult(
            refund.getRefundId(),
            refund.getUserId(),
            refund.getOrderId(),
            refund.getReason(),
            refund.getStatus(),
            refund.getCreatedAt()
        );
    }
}