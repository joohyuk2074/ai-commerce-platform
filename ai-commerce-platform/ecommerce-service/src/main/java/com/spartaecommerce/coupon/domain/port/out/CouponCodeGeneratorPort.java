package com.spartaecommerce.coupon.domain.port.out;

import java.util.Set;

/**
 * 쿠폰 코드 생성 포트 (Outbound Port)
 */
public interface CouponCodeGeneratorPort {

    /**
     * 고유한 쿠폰 코드를 생성합니다
     */
    String generate();

    /**
     * 중복되지 않는 고유한 쿠폰 코드 세트를 생성합니다
     */
    Set<String> generateUniqueCodes(int count);
}
