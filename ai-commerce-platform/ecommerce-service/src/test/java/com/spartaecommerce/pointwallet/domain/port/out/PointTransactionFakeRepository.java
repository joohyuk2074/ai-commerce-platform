package com.spartaecommerce.pointwallet.domain.port.out;

import com.spartaecommerce.pointwallet.domain.entity.PointTransaction;
import com.spartaecommerce.pointwallet.domain.entity.PointTransactionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PointTransactionFakeRepository implements LoadPointTransactionPort, SavePointTransactionPort {

    private final Map<Long, PointTransaction> repository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(PointTransaction transaction) {
        long transactionId = idGenerator.getAndIncrement();
        PointTransaction newTransaction = PointTransaction.builder()
            .transactionId(transactionId)
            .walletId(transaction.getWalletId())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .balanceAfter(transaction.getBalanceAfter())
            .description(transaction.getDescription())
            .expireAt(transaction.getExpireAt())
            .createdAt(LocalDateTime.now())
            .build();
        repository.put(transactionId, newTransaction);
        return transactionId;
    }

    @Override
    public List<PointTransaction> findByWalletId(Long walletId) {
        return repository.values().stream()
            .filter(transaction -> transaction.getWalletId().equals(walletId))
            .sorted(Comparator.comparing(PointTransaction::getCreatedAt).reversed())
            .toList();
    }

    @Override
    public List<PointTransaction> findExpiredTransactions(LocalDateTime now) {
        return repository.values().stream()
            .filter(transaction -> transaction.getType() == PointTransactionType.EARN)
            .filter(transaction -> transaction.getExpireAt() != null)
            .filter(transaction -> transaction.getExpireAt().isBefore(now) || transaction.getExpireAt().isEqual(now))
            .sorted(Comparator.comparing(PointTransaction::getCreatedAt))
            .toList();
    }

    public void clear() {
        repository.clear();
        idGenerator.set(1L);
    }
}
