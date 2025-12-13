package com.spartaecommerce.coupon.application.dto.result;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.entity.ScopeType;
import com.spartaecommerce.coupon.domain.value.DiscountValue;

import java.time.LocalDateTime;

public record CouponResult(
    Long couponId,
    String couponName,
    String discountType, // "PERCENT" or "FIXED"
    String discountValue, // "10%" for PERCENT, "10000" for FIXED
    Money minOrderAmount,
    Money maxDiscountAmount,
    ScopeType scopeType,
    Long scopeId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer usageLimit,
    Integer issuedCount,
    Integer usedCount,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CouponResult from(Coupon coupon) {
        DiscountValue discountValue = coupon.getDiscountValue();

        return new CouponResult(
            coupon.getCouponId(),
            coupon.getCouponName(),
            discountValue.getType().name(),
            discountValue.toString(),
            coupon.getMinOrderAmount(),
            coupon.getMaxDiscountAmount(),
            coupon.getScopeType(),
            coupon.getScopeId(),
            coupon.getStartDate(),
            coupon.getEndDate(),
            coupon.getUsageLimit(),
            coupon.getIssuedCount(),
            coupon.getUsedCount(),
            coupon.isDeleted(),
            coupon.getCreatedAt(),
            coupon.getUpdatedAt()
        );
    }
}
