package com.spartaecommerce.pointwallet.domain.port.out;

import com.spartaecommerce.pointwallet.domain.entity.PointTransaction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 거래 조회 포트 (Outbound Port)
 */
public interface LoadPointTransactionPort {

    List<PointTransaction> findByWalletId(Long walletId);

    List<PointTransaction> findExpiredTransactions(LocalDateTime now);
}
