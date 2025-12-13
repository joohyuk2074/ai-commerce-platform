package com.spartaecommerce.coupon.domain.port.out;

import com.spartaecommerce.coupon.domain.entity.Coupon;

/**
 * 쿠폰 저장 포트 (Outbound Port)
 */
public interface SaveCouponPort {

    /**
     * 쿠폰을 저장합니다
     *
     * @param coupon 저장할 쿠폰
     * @return 저장된 쿠폰 ID
     */
    Long save(Coupon coupon);
}
