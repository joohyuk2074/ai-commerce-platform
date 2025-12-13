package com.spartaecommerce.pointwallet.adapter.out.persistence;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.pointwallet.adapter.out.persistence.jpa.entity.PointWalletJpaEntity;
import com.spartaecommerce.pointwallet.adapter.out.persistence.jpa.repository.PointWalletJpaRepository;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.port.out.LoadPointWalletPort;
import com.spartaecommerce.pointwallet.domain.port.out.SavePointWalletPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointWalletPersistenceAdapter implements LoadPointWalletPort, SavePointWalletPort {

    private final PointWalletJpaRepository pointWalletJpaRepository;

    @Override
    public Long save(PointWallet wallet) {
        PointWalletJpaEntity walletJpaEntity = PointWalletJpaEntity.from(wallet);
        return pointWalletJpaRepository.save(walletJpaEntity).getWalletId();
    }

    @Override
    public Optional<PointWallet> findByUserId(Long userId) {
        return pointWalletJpaRepository.findByUserId(userId)
            .map(PointWalletJpaEntity::toDomain);
    }

    @Override
    public PointWallet getByUserId(Long userId) {
        return pointWalletJpaRepository.findByUserId(userId)
            .map(PointWalletJpaEntity::toDomain)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Wallet not found for userId: " + userId));
    }
}
