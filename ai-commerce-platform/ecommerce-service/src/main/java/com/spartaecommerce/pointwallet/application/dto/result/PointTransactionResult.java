package com.spartaecommerce.pointwallet.application.dto.result;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.pointwallet.domain.entity.PointTransaction;
import com.spartaecommerce.pointwallet.domain.entity.PointTransactionType;

import java.time.LocalDateTime;

public record PointTransactionResult(
    Long transactionId,
    PointTransactionType type,
    Money amount,
    Money balanceAfter,
    String description,
    LocalDateTime expireAt,
    LocalDateTime createdAt
) {
    public static PointTransactionResult from(PointTransaction transaction) {
        return new PointTransactionResult(
            transaction.getTransactionId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getBalanceAfter(),
            transaction.getDescription(),
            transaction.getExpireAt(),
            transaction.getCreatedAt()
        );
    }
}