package com.spartaecommerce.coupon.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spartaecommerce.coupon.application.dto.result.CouponResult;
import com.spartaecommerce.coupon.domain.entity.ScopeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
    Long couponId,
    String couponName,
    String discountType, // "PERCENT" or "FIXED"
    String discountValue, // "10%" for PERCENT, money amount for FIXED
    BigDecimal minOrderAmount,
    BigDecimal maxDiscountAmount,
    ScopeType scopeType,
    Long scopeId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endDate,
    Integer usageLimit,
    Integer issuedCount,
    Integer usedCount,
    boolean deleted,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {
    public static CouponResponse from(CouponResult result) {
        return new CouponResponse(
            result.couponId(),
            result.couponName(),
            result.discountType(),
            result.discountValue(),
            result.minOrderAmount().amount(),
            result.maxDiscountAmount() != null ? result.maxDiscountAmount().amount() : null,
            result.scopeType(),
            result.scopeId(),
            result.startDate(),
            result.endDate(),
            result.usageLimit(),
            result.issuedCount(),
            result.usedCount(),
            result.deleted(),
            result.createdAt(),
            result.updatedAt()
        );
    }
}
