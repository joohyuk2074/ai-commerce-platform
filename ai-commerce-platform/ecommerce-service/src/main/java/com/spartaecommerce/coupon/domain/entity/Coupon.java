package com.spartaecommerce.coupon.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.coupon.domain.value.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Coupon {

    private Long couponId;

    private String couponName;

    private DiscountValue discountValue;

    private Money minOrderAmount;

    private Money maxDiscountAmount;

    private CouponScope scope;

    private ValidityPeriod validityPeriod;

    private Integer usageLimit;

    private Integer issuedCount;

    private Integer usedCount;

    private boolean deleted;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Coupon createNew(
        String couponName,
        DiscountValue.DiscountType discountType,
        BigDecimal discountValue,
        Money minOrderAmount,
        Money maxDiscountAmount,
        ScopeType scopeType,
        Long scopeId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer usageLimit
    ) {
        validateCouponName(couponName);

        DiscountValue discount = createDiscountValue(discountType, discountValue);
        CouponScope scope = createCouponScope(scopeType, scopeId);
        ValidityPeriod validityPeriod = ValidityPeriod.of(startDate, endDate);

        validateDiscountValue(discount);
        validateMinOrderAmount(minOrderAmount);
        validateMaxDiscountAmount(maxDiscountAmount);
        validateUsageLimit(usageLimit);

        return Coupon.builder()
            .couponName(couponName)
            .discountValue(discount)
            .minOrderAmount(minOrderAmount)
            .maxDiscountAmount(maxDiscountAmount)
            .scope(scope)
            .validityPeriod(validityPeriod)
            .usageLimit(usageLimit)
            .issuedCount(0)
            .usedCount(0)
            .deleted(false)
            .build();
    }

    public void delete(LocalDateTime deletedAt) {
        this.deleted = true;
        this.deletedAt = deletedAt;
    }

    public void incrementUsedCount() {
        if (this.deleted) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot use deleted coupon. couponId: " + this.couponId
            );
        }

        if (this.usedCount >= this.usageLimit) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Coupon usage limit exceeded. couponId: " + this.couponId
            );
        }

        this.usedCount++;
    }

    /**
     * 주어진 금액에 대한 할인액을 계산합니다
     *
     * @param amount 할인 대상 금액
     * @return 할인액
     */
    public Money calculateDiscount(Money amount) {
        return this.discountValue.calculateDiscount(amount, this.maxDiscountAmount);
    }

    public boolean isActive(LocalDateTime now) {
        if (this.deleted) {
            return false;
        }

        if (!this.validityPeriod.isActiveAt(now)) {
            return false;
        }

        if (this.usedCount >= this.usageLimit) {
            return false;
        }

        return true;
    }

    public boolean canApplyToOrder(Money orderAmount, LocalDateTime now) {
        if (!isActive(now)) {
            return false;
        }

        return !orderAmount.isLessThan(this.minOrderAmount);
    }

    public boolean isPercentType() {
        return this.discountValue.getType() == DiscountValue.DiscountType.PERCENT;
    }

    public DiscountValue.DiscountType getDiscountType() {
        return this.discountValue.getType();
    }

    // CouponScope 편의 메서드
    public ScopeType getScopeType() {
        return this.scope.scopeType();
    }

    public Long getScopeId() {
        return this.scope.scopeId();
    }

    // ValidityPeriod 편의 메서드
    public LocalDateTime getStartDate() {
        return this.validityPeriod.startDate();
    }

    public LocalDateTime getEndDate() {
        return this.validityPeriod.endDate();
    }

    private static void validateCouponName(String couponName) {
        if (couponName == null || couponName.isBlank()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Coupon name must not be null or blank"
            );
        }

        if (couponName.length() > 100) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Coupon name must not exceed 100 characters"
            );
        }
    }

    private static void validateDiscountValue(DiscountValue discountValue) {
        if (discountValue == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Discount value must not be null"
            );
        }

        discountValue.validate();
    }

    private static void validateMinOrderAmount(Money minOrderAmount) {
        if (minOrderAmount == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Minimum order amount must not be null"
            );
        }

        // Money constructor already validates non-negative
    }

    private static void validateMaxDiscountAmount(Money maxDiscountAmount) {
        if (maxDiscountAmount == null) {
            return; // null is allowed (means no limit)
        }

        if (maxDiscountAmount.isZero()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Maximum discount amount must be greater than 0"
            );
        }
    }


    private static void validateUsageLimit(Integer usageLimit) {
        if (usageLimit == null || usageLimit <= 0) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Usage limit must be greater than 0"
            );
        }
    }

    /**
     * 할인 타입과 값으로부터 DiscountValue 도메인 객체를 생성합니다
     */
    private static DiscountValue createDiscountValue(
        DiscountValue.DiscountType discountType,
        BigDecimal discountValue
    ) {
        if (discountType == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Discount type must not be null or blank"
            );
        }

        if (discountValue == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Discount value must not be null"
            );
        }

        return switch (discountType) {
            case DiscountValue.DiscountType.PERCENT -> PercentageDiscount.of(discountValue.intValue());
            case DiscountValue.DiscountType.FIXED -> FixedDiscount.of(Money.from(discountValue));
        };
    }

    /**
     * 스코프 타입과 ID로부터 CouponScope 도메인 객체를 생성합니다
     */
    private static CouponScope createCouponScope(ScopeType scopeType, Long scopeId) {
        if (scopeType == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Scope type must not be null"
            );
        }

        return switch (scopeType) {
            case ALL -> CouponScope.forAllProducts();
            case CATEGORY -> CouponScope.forCategory(scopeId);
            case PRODUCT -> CouponScope.forProduct(scopeId);
        };
    }
}
