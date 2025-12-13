package com.spartaecommerce.coupon.domain.port.in;

import com.spartaecommerce.coupon.application.dto.result.CouponUserResult;

/**
 * 쿠폰 사용자 조회 유스케이스
 */
public interface GetCouponUserUseCase {

    /**
     * 쿠폰 코드로 쿠폰을 조회합니다
     *
     * @param code 쿠폰 코드
     * @return 쿠폰 정보
     */
    CouponUserResult getByCode(String code);
}
