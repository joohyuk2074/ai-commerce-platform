package com.spartaecommerce.pointwallet.domain.entity;

import com.spartaecommerce.common.domain.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PointTransaction {

    private Long transactionId;

    private Long walletId;

    private PointTransactionType type;

    private Money amount;

    private Money balanceAfter;  // 거래 후 잔액

    private String description;

    private LocalDateTime expireAt;  // 포인트 만료일 (EARN 타입에만 적용)

    private LocalDateTime createdAt;

    /**
     * 포인트 적립 거래 생성
     */
    public static PointTransaction createEarn(
        Long walletId,
        Money amount,
        Money balanceAfter,
        LocalDateTime expireAt,
        String description
    ) {
        return PointTransaction.builder()
            .walletId(walletId)
            .type(PointTransactionType.EARN)
            .amount(amount)
            .balanceAfter(balanceAfter)
            .expireAt(expireAt)
            .description(description)
            .build();
    }

    /**
     * 포인트 사용 거래 생성
     */
    public static PointTransaction createUse(
        Long walletId,
        Money amount,
        Money balanceAfter,
        String description
    ) {
        return PointTransaction.builder()
            .walletId(walletId)
            .type(PointTransactionType.USE)
            .amount(amount)
            .balanceAfter(balanceAfter)
            .description(description)
            .build();
    }

    /**
     * 포인트 만료 거래 생성
     */
    public static PointTransaction createExpire(
        Long walletId,
        Money amount,
        Money balanceAfter,
        String description
    ) {
        return PointTransaction.builder()
            .walletId(walletId)
            .type(PointTransactionType.EXPIRE)
            .amount(amount)
            .balanceAfter(balanceAfter)
            .description(description)
            .build();
    }

    public boolean isExpired() {
        if (expireAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireAt);
    }
}