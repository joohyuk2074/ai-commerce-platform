package com.spartaecommerce.pointwallet.application.dto.result;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;

import java.time.LocalDateTime;

public record PointWalletResult(
    Long walletId,
    Long userId,
    Money balance,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PointWalletResult from(PointWallet wallet) {
        return new PointWalletResult(
            wallet.getWalletId(),
            wallet.getUserId(),
            wallet.getBalance(),
            wallet.isActive(),
            wallet.getCreatedAt(),
            wallet.getUpdatedAt()
        );
    }
}