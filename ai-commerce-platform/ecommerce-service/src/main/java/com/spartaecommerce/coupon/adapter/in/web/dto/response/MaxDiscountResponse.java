package com.spartaecommerce.coupon.adapter.in.web.dto.response;

import com.spartaecommerce.coupon.application.dto.result.MaxDiscountResult;

import java.math.BigDecimal;

public record MaxDiscountResponse(
    BigDecimal maxDiscountRate,
    Long couponId,
    String couponName,
    String discountType, // "PERCENT" or "FIXED"
    String discountValue, // "10%" or money amount
    BigDecimal calculatedDiscountAmount
) {
    public static MaxDiscountResponse from(MaxDiscountResult result) {
        return new MaxDiscountResponse(
            result.maxDiscountRate(),
            result.couponId(),
            result.couponName(),
            result.discountType(),
            result.discountValue(),
            result.calculatedDiscountAmount().amount()
        );
    }
}
