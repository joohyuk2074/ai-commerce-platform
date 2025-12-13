package com.spartaecommerce.pointwallet.adapter.in.web.dto.response;

import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.domain.entity.PointTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointTransactionResponse(
    Long transactionId,
    PointTransactionType type,
    BigDecimal amount,
    BigDecimal balanceAfter,
    String description,
    LocalDateTime expireAt,
    LocalDateTime createdAt
) {

    public static PointTransactionResponse from(PointTransactionResult result) {
        return new PointTransactionResponse(
            result.transactionId(),
            result.type(),
            result.amount().amount(),
            result.balanceAfter().amount(),
            result.description(),
            result.expireAt(),
            result.createdAt()
        );
    }
}
