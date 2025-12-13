package com.spartaecommerce.pointwallet.domain.port.out;

import com.spartaecommerce.pointwallet.domain.entity.PointTransaction;

/**
 * 포인트 거래 저장 포트 (Outbound Port)
 */
public interface SavePointTransactionPort {

    Long save(PointTransaction transaction);
}
