package com.spartaecommerce.coupon.adapter.out.service;

import com.spartaecommerce.coupon.domain.port.out.CouponCodeGeneratorPort;
import com.spartaecommerce.coupon.domain.service.CouponCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 쿠폰 코드 생성 어댑터
 * Outbound port (CouponCodeGeneratorPort)를 구현합니다
 */
@Component
@RequiredArgsConstructor
public class CouponCodeGeneratorAdapter implements CouponCodeGeneratorPort {

    private final CouponCodeGenerator couponCodeGenerator;

    @Override
    public String generate() {
        return couponCodeGenerator.generate();
    }

    @Override
    public Set<String> generateUniqueCodes(int count) {
        return couponCodeGenerator.generateUniqueCodes(count);
    }
}
