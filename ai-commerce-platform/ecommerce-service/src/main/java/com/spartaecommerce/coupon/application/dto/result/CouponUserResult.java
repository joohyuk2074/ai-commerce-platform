package com.spartaecommerce.coupon.application.dto.result;

import com.spartaecommerce.coupon.domain.entity.CouponStatus;
import com.spartaecommerce.coupon.domain.entity.CouponUser;

import java.time.LocalDateTime;

/**
 * 사용자 쿠폰 조회 결과
 */
public record CouponUserResult(
        Long couponUserId,
        Long userId,
        Long couponId,
        String code,
        CouponStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CouponUserResult from(CouponUser couponUser) {
        return new CouponUserResult(
                couponUser.getCouponUserId(),
                couponUser.getUserId(),
                couponUser.getCouponId(),
                couponUser.getCode(),
                couponUser.getStatus(),
                couponUser.getCreatedAt(),
                couponUser.getUpdatedAt()
        );
    }
}
