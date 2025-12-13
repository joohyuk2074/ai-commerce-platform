package com.spartaecommerce.refund.adapter.in.web.dto.request;

import com.spartaecommerce.refund.application.dto.command.RefundProcessCommand;
import com.spartaecommerce.refund.domain.entity.RefundStatus;
import jakarta.validation.constraints.NotNull;

public record RefundProcessRequest(
    @NotNull
    RefundStatus status
) {
    public RefundProcessCommand toCommand(Long refundId) {
        return new RefundProcessCommand(refundId, status);
    }
}
