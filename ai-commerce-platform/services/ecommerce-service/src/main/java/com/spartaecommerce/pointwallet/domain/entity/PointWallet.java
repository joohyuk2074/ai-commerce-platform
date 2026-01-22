package com.spartaecommerce.pointwallet.domain.entity;

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
public class PointWallet {

    private Long walletId;

    private Long userId;

    private Money balance;  // 포인트 잔액

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static PointWallet createNew(Long userId) {
        return PointWallet.builder()
            .userId(userId)
            .balance(Money.ZERO)
            .active(true)
            .build();
    }

    public void deactivate() {
        this.active = false;
    }

    public void earnPoints(Money amount, Money maxBalance) {
        validateActive();
        validatePositiveAmount(amount);

        Money newBalance = this.balance.add(amount);

        if (newBalance.isGreaterThan(Money.from(maxBalance.amount()))) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Exceeds maximum balance limit: " + maxBalance.amount()
            );
        }

        this.balance = newBalance;
    }

    /**
     * 포인트 사용
     */
    public void usePoints(Money amount, Integer minUsagePoint) {
        validateActive();
        validatePositiveAmount(amount);

        // 최소 사용 포인트 검증
        if (amount.amount().intValue() < minUsagePoint) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Minimum usage point is " + minUsagePoint + ". Requested: " + amount.amount()
            );
        }

        // 잔액 부족 검증
        if (this.balance.isLessThan(amount)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Insufficient balance. Available: " + this.balance.amount() + ", Requested: " + amount.amount()
            );
        }

        this.balance = this.balance.subtract(amount);
    }

    /**
     * 포인트 만료 (차감)
     */
    public void expirePoints(Money amount) {
        validateActive();
        validatePositiveAmount(amount);

        if (this.balance.isLessThan(amount)) {
            // 만료 시에는 잔액보다 많이 차감하지 않고 전액 차감
            this.balance = Money.ZERO;
        } else {
            this.balance = this.balance.subtract(amount);
        }
    }

    private void validateActive() {
        if (!this.active) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Wallet is not active"
            );
        }
    }

    private void validatePositiveAmount(Money amount) {
        if (amount == null || amount.isZero() || amount.isLessThan(Money.ZERO)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Amount must be positive"
            );
        }
    }
}