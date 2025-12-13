package com.spartaecommerce.coupon.application.service;

import com.spartaecommerce.common.domain.CustomPageable;
import com.spartaecommerce.coupon.application.dto.query.CouponSearchQuery;
import com.spartaecommerce.coupon.application.dto.result.CouponResult;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.port.in.CouponQueryUseCase;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 쿠폰 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponQueryService implements CouponQueryUseCase {

    private final LoadCouponPort loadCouponPort;

    @Override
    public CouponResult getById(Long couponId) {
        Coupon coupon = loadCouponPort.getById(couponId);
        return CouponResult.from(coupon);
    }

    @Override
    public Page<CouponResult> search(Boolean isActive, CustomPageable pageable) {
        CouponSearchQuery query = new CouponSearchQuery(
            isActive,
            LocalDateTime.now(),
            pageable
        );

        return loadCouponPort.search(query)
            .map(CouponResult::from);
    }
}
