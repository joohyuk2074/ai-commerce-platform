package com.spartaecommerce.coupon.domain.value;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;

import java.math.BigDecimal;

/**
 * 정액(고정 금액) 할인을 나타내는 Value Object
 */
public record FixedDiscount(
    Money amount
) implements DiscountValue {

    private static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(0.01);

    public static FixedDiscount of(Money amount) {
        FixedDiscount discount = new FixedDiscount(amount);
        discount.validate();
        return discount;
    }

    @Override
    public Money calculateDiscount(Money orderAmount, Money maxDiscountAmount) {
        if (orderAmount == null) {
            throw new IllegalArgumentException("Order amount must not be null");
        }

        // 정액 할인은 고정 금액 반환
        // 단, 주문 금액보다 클 수 없음
        if (amount.isGreaterThan(orderAmount)) {
            return orderAmount;
        }

        return amount;
    }

    @Override
    public DiscountType getType() {
        return DiscountType.FIXED;
    }

    @Override
    public void validate() {
        if (amount == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Discount amount must not be null"
            );
        }

        if (amount.amount().compareTo(MIN_AMOUNT) < 0) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                String.format("Discount amount must be at least %s", MIN_AMOUNT)
            );
        }
    }

    @Override
    public String toStorageString() {
        return "FIXED:" + amount.amount().toPlainString();
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
