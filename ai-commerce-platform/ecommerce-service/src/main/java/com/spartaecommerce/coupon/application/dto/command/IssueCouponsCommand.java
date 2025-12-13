package com.spartaecommerce.coupon.application.dto.command;

/**
 * 쿠폰 대량 발급 커맨드
 *
 * @param couponId 쿠폰 ID
 * @param quantity 발급 수량
 */
public record IssueCouponsCommand(
    Long couponId,
    Integer quantity
) {
}
