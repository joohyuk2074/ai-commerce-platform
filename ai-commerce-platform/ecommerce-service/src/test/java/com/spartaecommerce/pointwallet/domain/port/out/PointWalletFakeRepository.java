package com.spartaecommerce.pointwallet.domain.port.out;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PointWalletFakeRepository implements LoadPointWalletPort, SavePointWalletPort {

    private final Map<Long, PointWallet> walletRepository = new ConcurrentHashMap<>();
    private final Map<Long, PointWallet> userIdIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(PointWallet wallet) {
        if (wallet.getWalletId() == null) {
            long walletId = idGenerator.getAndIncrement();
            PointWallet newWallet = PointWallet.builder()
                .walletId(walletId)
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .active(wallet.isActive())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            walletRepository.put(walletId, newWallet);
            userIdIndex.put(wallet.getUserId(), newWallet);
            return walletId;
        } else {
            PointWallet updatedWallet = PointWallet.builder()
                .walletId(wallet.getWalletId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .active(wallet.isActive())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            walletRepository.put(wallet.getWalletId(), updatedWallet);
            userIdIndex.put(wallet.getUserId(), updatedWallet);
            return wallet.getWalletId();
        }
    }

    @Override
    public Optional<PointWallet> findByUserId(Long userId) {
        return Optional.ofNullable(userIdIndex.get(userId));
    }

    @Override
    public PointWallet getByUserId(Long userId) {
        return findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Wallet not found for userId: " + userId));
    }

    public void clear() {
        walletRepository.clear();
        userIdIndex.clear();
        idGenerator.set(1L);
    }
}
