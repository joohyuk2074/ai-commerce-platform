package com.spartaecommerce.refund.domain.entity;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Refund {

    private Long refundId;

    private Long userId;

    private Long orderId;

    private String reason;

    private RefundStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Refund createNew(Long userId, Long orderId, String reason) {
        return Refund.builder()
            .userId(userId)
            .orderId(orderId)
            .reason(reason)
            .status(RefundStatus.PENDING)
            .build();
    }

    public void approve() {
        validateStatusTransition(this.status, RefundStatus.APPROVED);
        this.status = RefundStatus.APPROVED;
    }

    public void reject() {
        validateStatusTransition(this.status, RefundStatus.REJECTED);
        this.status = RefundStatus.REJECTED;
    }

    private void validateStatusTransition(RefundStatus from, RefundStatus to) {
        if (from == to) {
            throw new BusinessException(
                ErrorCode.REFUND_INVALID_STATE_TRANSITION,
                from + " -> " + to
            );
        }

        if (from != RefundStatus.PENDING) {
            throw new BusinessException(
                ErrorCode.REFUND_INVALID_STATE_TRANSITION,
                from + " -> " + to
            );
        }
    }
}