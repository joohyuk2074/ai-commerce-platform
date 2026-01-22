package com.spartaecommerce.pointwallet.domain.port.out;

import com.spartaecommerce.common.domain.pointwallet.PointWallet;

/**
 * 포인트 지갑 저장 포트 (Outbound Port)
 */
public interface SavePointWalletPort {

    Long save(PointWallet wallet);
}
