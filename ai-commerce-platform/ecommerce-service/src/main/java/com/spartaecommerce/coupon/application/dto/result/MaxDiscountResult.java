package com.spartaecommerce.coupon.application.dto.result;

import com.spartaecommerce.common.domain.Money;

import java.math.BigDecimal;

public record MaxDiscountResult(
    BigDecimal maxDiscountRate,
    Long couponId,
    String couponName,
    String discountType, // "PERCENT" or "FIXED"
    String discountValue, // "10%" or money amount
    Money calculatedDiscountAmount
) {
}
