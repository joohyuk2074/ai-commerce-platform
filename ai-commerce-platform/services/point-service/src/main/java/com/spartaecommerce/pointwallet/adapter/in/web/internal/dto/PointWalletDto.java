package com.spartaecommerce.pointwallet.adapter.in.web.internal.dto;

import com.spartaecommerce.common.domain.pointwallet.PointWallet;

import java.math.BigDecimal;

public record PointWalletDto(
    Long walletId,
    Long userId,
    BigDecimal balance,
    boolean active
) {
    public static PointWalletDto from(PointWallet wallet) {
        return new PointWalletDto(
            wallet.getWalletId(),
            wallet.getUserId(),
            wallet.getBalance().amount(),
            wallet.isActive()
        );
    }
}
