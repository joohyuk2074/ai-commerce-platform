package com.spartaecommerce.coupon.application.service;

import com.spartaecommerce.coupon.application.dto.result.CouponUserResult;
import com.spartaecommerce.coupon.domain.entity.CouponUser;
import com.spartaecommerce.coupon.domain.port.in.GetCouponUserUseCase;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 쿠폰 사용자 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCouponUserService implements GetCouponUserUseCase {

    private final LoadCouponUserPort loadCouponUserPort;

    @Override
    public CouponUserResult getByCode(String code) {
        CouponUser couponUser = loadCouponUserPort.getByCode(code);
        return CouponUserResult.from(couponUser);
    }
}
