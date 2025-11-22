package com.spartaecommerce.wallet.domain.repository;

import com.spartaecommerce.pointwallet.domain.entity.PointTransaction;
import com.spartaecommerce.pointwallet.domain.entity.PointTransactionType;
import com.spartaecommerce.pointwallet.domain.repository.PointTransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PointTransactionFakeRepository implements PointTransactionRepository {

    private final Map<Long, PointTransaction> repository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(PointTransaction transaction) {
        long transactionId = idGenerator.getAndIncrement();
        PointTransaction savedTransaction = PointTransaction.builder()
            .transactionId(transactionId)
            .walletId(transaction.getWalletId())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .balanceAfter(transaction.getBalanceAfter())
            .description(transaction.getDescription())
            .expireAt(transaction.getExpireAt())
            .createdAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt() : LocalDateTime.now())
            .build();
        repository.put(transactionId, savedTransaction);
        return transactionId;
    }

    @Override
    public List<PointTransaction> findByWalletId(Long walletId) {
        return repository.values().stream()
            .filter(transaction -> transaction.getWalletId().equals(walletId))
            .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
            .toList();
    }

    @Override
    public List<PointTransaction> findExpiredTransactions(LocalDateTime now) {
        return repository.values().stream()
            .filter(transaction -> transaction.getType() == PointTransactionType.EARN)
            .filter(transaction -> transaction.getExpireAt() != null)
            .filter(transaction -> transaction.getExpireAt().isBefore(now) || transaction.getExpireAt().isEqual(now))
            .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
            .toList();
    }

    public void clear() {
        repository.clear();
    }
}