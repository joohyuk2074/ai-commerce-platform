package com.spartaecommerce.refund.adapter.in.web.dto.request;

import com.spartaecommerce.refund.application.dto.command.RefundCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefundCreateRequest(
    @NotNull
    Long userId,

    @NotNull
    Long orderId,

    @NotBlank
    String reason
) {
    public RefundCreateCommand toCommand() {
        return new RefundCreateCommand(userId, orderId, reason);
    }
}
