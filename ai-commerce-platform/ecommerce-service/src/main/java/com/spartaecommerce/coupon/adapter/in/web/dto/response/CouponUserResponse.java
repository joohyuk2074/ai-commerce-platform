package com.spartaecommerce.coupon.adapter.in.web.dto.response;

import com.spartaecommerce.coupon.application.dto.result.CouponUserResult;
import com.spartaecommerce.coupon.domain.entity.CouponStatus;

import java.time.LocalDateTime;

/**
 * 사용자 쿠폰 응답
 */
public record CouponUserResponse(
        Long couponUserId,
        Long userId,
        Long couponId,
        String code,
        CouponStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CouponUserResponse from(CouponUserResult result) {
        return new CouponUserResponse(
                result.couponUserId(),
                result.userId(),
                result.couponId(),
                result.code(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
