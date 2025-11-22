package com.spartaecommerce.refund.application.dto.command;

import com.spartaecommerce.refund.domain.entity.RefundStatus;

public record RefundProcessCommand(
    Long refundId,
    RefundStatus status
) {
}