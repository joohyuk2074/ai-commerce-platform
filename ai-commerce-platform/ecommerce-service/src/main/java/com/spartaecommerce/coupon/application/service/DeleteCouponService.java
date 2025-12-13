package com.spartaecommerce.coupon.application.service;

import com.spartaecommerce.common.util.DateTimeHolder;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.port.in.DeleteCouponUseCase;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponPort;
import com.spartaecommerce.coupon.domain.port.out.SaveCouponPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 쿠폰 삭제 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DeleteCouponService implements DeleteCouponUseCase {

    private final LoadCouponPort loadCouponPort;
    private final SaveCouponPort saveCouponPort;
    private final DateTimeHolder dateTimeHolder;

    @Override
    public void delete(Long couponId) {
        Coupon coupon = loadCouponPort.getById(couponId);
        coupon.delete(dateTimeHolder.getCurrentDateTime());
        saveCouponPort.save(coupon);
    }
}
