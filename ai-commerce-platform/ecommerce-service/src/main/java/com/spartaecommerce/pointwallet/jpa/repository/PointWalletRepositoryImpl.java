package com.spartaecommerce.pointwallet.jpa.repository;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.repository.PointWalletRepository;
import com.spartaecommerce.pointwallet.jpa.entity.PointWalletJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointWalletRepositoryImpl implements PointWalletRepository {

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