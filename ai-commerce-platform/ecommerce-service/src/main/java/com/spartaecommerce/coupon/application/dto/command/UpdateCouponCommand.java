package com.spartaecommerce.coupon.application.dto.command;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.coupon.domain.entity.ScopeType;
import com.spartaecommerce.coupon.domain.value.DiscountValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateCouponCommand(
    String couponName,
    DiscountValue.DiscountType discountType, // "PERCENT" or "FIXED"
    BigDecimal discountValue, // percentage (1-100) for PERCENT, amount for FIXED
    Money minOrderAmount,
    Money maxDiscountAmount,
    ScopeType scopeType,
    Long scopeId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer usageLimit
) {
}
