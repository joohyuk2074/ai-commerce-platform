package com.spartaecommerce.adapter.out.external.dto;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.domain.pointwallet.PointWallet;

import java.math.BigDecimal;

public record PointWalletDto(
    Long walletId,
    Long userId,
    BigDecimal balance,
    boolean active
) {
    public PointWallet toDomain() {
        return PointWallet.builder()
            .walletId(walletId)
            .userId(userId)
            .balance(Money.from(balance))
            .active(active)
            .build();
    }
}
