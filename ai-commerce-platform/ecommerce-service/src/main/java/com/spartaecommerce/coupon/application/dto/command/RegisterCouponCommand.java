package com.spartaecommerce.coupon.application.dto.command;

/**
 * 쿠폰 등록 커맨드
 *
 * @param couponCode 쿠폰 코드
 * @param userId 사용자 ID
 */
public record RegisterCouponCommand(
        String couponCode,
        Long userId
) {
}
