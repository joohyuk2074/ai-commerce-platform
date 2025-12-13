package com.spartaecommerce.coupon.domain.port.in;

/**
 * 쿠폰 삭제 유스케이스
 */
public interface DeleteCouponUseCase {

    /**
     * 쿠폰을 삭제합니다 (소프트 삭제)
     *
     * @param couponId 쿠폰 ID
     */
    void delete(Long couponId);
}
