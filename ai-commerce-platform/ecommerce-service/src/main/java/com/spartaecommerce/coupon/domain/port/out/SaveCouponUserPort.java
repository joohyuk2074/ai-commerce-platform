package com.spartaecommerce.coupon.domain.port.out;

import com.spartaecommerce.coupon.domain.entity.CouponUser;

import java.util.List;

/**
 * 쿠폰 사용자 저장 포트 (Outbound Port)
 */
public interface SaveCouponUserPort {

    /**
     * 쿠폰 사용자를 저장합니다
     */
    Long save(CouponUser couponUser);

    /**
     * 여러 쿠폰 사용자를 한 번에 저장합니다
     */
    List<Long> saveAll(List<CouponUser> couponUsers);
}
