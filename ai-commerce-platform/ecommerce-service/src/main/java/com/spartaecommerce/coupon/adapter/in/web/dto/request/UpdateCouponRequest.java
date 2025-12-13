package com.spartaecommerce.coupon.adapter.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.coupon.application.dto.command.UpdateCouponCommand;
import com.spartaecommerce.coupon.domain.entity.ScopeType;
import com.spartaecommerce.coupon.domain.value.DiscountValue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateCouponRequest(
    @Size(max = 100, message = "Coupon name must not exceed 100 characters")
    String couponName,

    @Pattern(regexp = "PERCENT|FIXED", message = "Discount type must be either PERCENT or FIXED")
    DiscountValue.DiscountType discountType,

    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    BigDecimal discountValue,

    @DecimalMin(value = "0", message = "Minimum order amount must not be negative")
    BigDecimal minOrderAmount,

    @DecimalMin(value = "0.01", message = "Maximum discount amount must be greater than 0")
    BigDecimal maxDiscountAmount,

    ScopeType scopeType,

    Long scopeId,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startDate,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endDate,

    @Min(value = 1, message = "Usage limit must be at least 1")
    Integer usageLimit
) {
    public UpdateCouponCommand toCommand() {
        return new UpdateCouponCommand(
            couponName,
            discountType,
            discountValue,
            minOrderAmount != null ? Money.from(minOrderAmount) : null,
            maxDiscountAmount != null ? Money.from(maxDiscountAmount) : null,
            scopeType,
            scopeId,
            startDate,
            endDate,
            usageLimit
        );
    }
}
