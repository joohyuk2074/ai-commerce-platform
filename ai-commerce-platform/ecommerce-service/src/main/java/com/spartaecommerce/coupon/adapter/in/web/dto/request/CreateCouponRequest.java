package com.spartaecommerce.coupon.adapter.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.coupon.application.dto.command.CreateCouponCommand;
import com.spartaecommerce.coupon.domain.entity.ScopeType;
import com.spartaecommerce.coupon.domain.value.DiscountValue;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCouponRequest(
    @NotBlank(message = "Coupon name must not be blank")
    @Size(max = 100, message = "Coupon name must not exceed 100 characters")
    String couponName,

    @NotNull(message = "Discount type must not be null")
    @Pattern(regexp = "PERCENT|FIXED", message = "Discount type must be either PERCENT or FIXED")
    DiscountValue.DiscountType discountType,

    @NotNull(message = "Discount value must not be null")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    BigDecimal discountValue,

    @NotNull(message = "Minimum order amount must not be null")
    @DecimalMin(value = "0", message = "Minimum order amount must not be negative")
    BigDecimal minOrderAmount,

    @DecimalMin(value = "0.01", message = "Maximum discount amount must be greater than 0")
    BigDecimal maxDiscountAmount,

    @NotNull(message = "Scope type must not be null")
    ScopeType scopeType,

    Long scopeId,

    @NotNull(message = "Start date must not be null")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startDate,

    @NotNull(message = "End date must not be null")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endDate,

    @NotNull(message = "Usage limit must not be null")
    @Min(value = 1, message = "Usage limit must be at least 1")
    Integer usageLimit
) {
    public CreateCouponCommand toCommand() {
        return new CreateCouponCommand(
            couponName,
            discountType,
            discountValue,
            Money.from(minOrderAmount),
            maxDiscountAmount != null ? Money.from(maxDiscountAmount) : null,
            scopeType,
            scopeId,
            startDate,
            endDate,
            usageLimit
        );
    }
}
