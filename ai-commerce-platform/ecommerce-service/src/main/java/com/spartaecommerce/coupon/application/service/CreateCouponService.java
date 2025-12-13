package com.spartaecommerce.coupon.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.coupon.application.dto.command.CreateCouponCommand;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.port.in.CreateCouponUseCase;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponPort;
import com.spartaecommerce.coupon.domain.port.out.SaveCouponPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 쿠폰 생성 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateCouponService implements CreateCouponUseCase {

    private final LoadCouponPort loadCouponPort;
    private final SaveCouponPort saveCouponPort;

    @Override
    public Long create(CreateCouponCommand command) {
        validateCouponNameUnique(command.couponName());

        Coupon coupon = Coupon.createNew(
            command.couponName(),
            command.discountType(),
            command.discountValue(),
            command.minOrderAmount(),
            command.maxDiscountAmount(),
            command.scopeType(),
            command.scopeId(),
            command.startDate(),
            command.endDate(),
            command.usageLimit()
        );

        return saveCouponPort.save(coupon);
    }

    private void validateCouponNameUnique(String couponName) {
        if (loadCouponPort.existsByName(couponName)) {
            throw new BusinessException(
                ErrorCode.ENTITY_ALREADY_EXISTS,
                "Coupon with the same name already exists: " + couponName
            );
        }
    }
}
