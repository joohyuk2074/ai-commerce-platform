package com.spartaecommerce.pointwallet.fixture;

import com.spartaecommerce.common.domain.pointwallet.PointWallet;
import com.spartaecommerce.pointwallet.domain.port.out.SavePointWalletPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트용 SavePointWalletPort Fake 구현체
 * 메모리 기반으로 포인트 지갑 저장 기능을 제공합니다.
 */
public class FakeSavePointWalletPort implements SavePointWalletPort {
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, PointWallet> walletsById = new HashMap<>();
    private final Map<Long, PointWallet> walletsByUserId = new HashMap<>();

    @Override
    public Long save(PointWallet wallet) {
        Long walletId = wallet.getWalletId();
        if (walletId == null) {
            walletId = idGenerator.getAndIncrement();
        }

        PointWallet savedWallet = PointWallet.builder()
            .walletId(walletId)
            .userId(wallet.getUserId())
            .balance(wallet.getBalance())
            .active(wallet.isActive())
            .createdAt(wallet.getCreatedAt())
            .updatedAt(wallet.getUpdatedAt())
            .build();

        walletsById.put(walletId, savedWallet);
        walletsByUserId.put(wallet.getUserId(), savedWallet);
        return walletId;
    }

    /**
     * 사용자 ID로 포인트 지갑을 조회합니다.
     */
    public Optional<PointWallet> findByUserId(Long userId) {
        return Optional.ofNullable(walletsByUserId.get(userId));
    }

    /**
     * 저장된 모든 포인트 지갑을 반환합니다.
     */
    public Map<Long, PointWallet> getAllWallets() {
        return new HashMap<>(walletsById);
    }
}
