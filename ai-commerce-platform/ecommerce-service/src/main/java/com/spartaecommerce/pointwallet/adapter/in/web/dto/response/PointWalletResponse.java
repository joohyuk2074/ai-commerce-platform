package com.spartaecommerce.pointwallet.adapter.in.web.dto.response;

import com.spartaecommerce.pointwallet.application.dto.result.PointWalletResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointWalletResponse(
    Long walletId,
    Long userId,
    BigDecimal balance,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static PointWalletResponse from(PointWalletResult result) {
        return new PointWalletResponse(
            result.walletId(),
            result.userId(),
            result.balance().amount(),
            result.active(),
            result.createdAt(),
            result.updatedAt()
        );
    }
}
