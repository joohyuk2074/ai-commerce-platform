package com.spartaecommerce.pointwallet.domain.port.out;

import com.spartaecommerce.common.domain.pointwallet.PointWallet;

import java.util.Optional;

/**
 * 포인트 지갑 조회 포트 (Outbound Port)
 */
public interface LoadPointWalletPort {

    Optional<PointWallet> findByUserId(Long userId);

    PointWallet getByUserId(Long userId);
}
