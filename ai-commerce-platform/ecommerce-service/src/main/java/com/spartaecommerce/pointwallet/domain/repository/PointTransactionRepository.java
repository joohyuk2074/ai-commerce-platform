package com.spartaecommerce.pointwallet.domain.repository;

import com.spartaecommerce.pointwallet.domain.entity.PointTransaction;

import java.time.LocalDateTime;
import java.util.List;

public interface PointTransactionRepository {

    Long save(PointTransaction transaction);

    List<PointTransaction> findByWalletId(Long walletId);

    List<PointTransaction> findExpiredTransactions(LocalDateTime now);
}