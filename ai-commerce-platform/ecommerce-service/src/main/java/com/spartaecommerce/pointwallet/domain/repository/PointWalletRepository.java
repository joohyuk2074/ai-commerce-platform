package com.spartaecommerce.pointwallet.domain.repository;

import com.spartaecommerce.pointwallet.domain.entity.PointWallet;

import java.util.Optional;

public interface PointWalletRepository {

    Long save(PointWallet wallet);

    Optional<PointWallet> findByUserId(Long userId);

    PointWallet getByUserId(Long userId);
}