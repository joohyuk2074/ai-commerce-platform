package com.spartaecommerce.coupon.domain.value;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 정률(%) 할인을 나타내는 Value Object
 *
 * @param percentage 1~100
 */
public record PercentageDiscount(
    int percentage
) implements DiscountValue {

    private static final int MIN_PERCENTAGE = 1;
    private static final int MAX_PERCENTAGE = 100;

    public static PercentageDiscount of(int percentage) {
        PercentageDiscount discount = new PercentageDiscount(percentage);
        discount.validate();
        return discount;
    }

    @Override
    public Money calculateDiscount(Money orderAmount, Money maxDiscountAmount) {
        if (orderAmount == null) {
            throw new IllegalArgumentException("Order amount must not be null");
        }

        // 백분율 할인 계산: orderAmount * (percentage / 100)
        BigDecimal discountAmount = orderAmount.amount()
            .multiply(BigDecimal.valueOf(percentage))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);

        Money calculatedDiscount = Money.from(discountAmount);

        // 최대 할인 금액 적용
        if (maxDiscountAmount != null && calculatedDiscount.isGreaterThan(maxDiscountAmount)) {
            return maxDiscountAmount;
        }

        return calculatedDiscount;
    }

    @Override
    public DiscountType getType() {
        return DiscountType.PERCENT;
    }

    @Override
    public void validate() {
        if (percentage < MIN_PERCENTAGE || percentage > MAX_PERCENTAGE) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                String.format("Percentage must be between %d and %d, but was %d",
                    MIN_PERCENTAGE, MAX_PERCENTAGE, percentage)
            );
        }
    }

    @Override
    public String toStorageString() {
        return "PERCENT:" + percentage;
    }

    @Override
    public String toString() {
        return percentage + "%";
    }
}
