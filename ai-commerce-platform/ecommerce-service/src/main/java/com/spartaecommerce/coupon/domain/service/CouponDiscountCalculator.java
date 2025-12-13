package com.spartaecommerce.coupon.domain.service;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.value.DiscountValue;
import com.spartaecommerce.coupon.domain.value.PercentageDiscount;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CouponDiscountCalculator {

    /**
     * Calculate the actual discount amount for a given order amount and coupon
     */
    public Money calculateDiscountAmount(Coupon coupon, Money productAmount) {
        return coupon.calculateDiscount(productAmount);
    }

    /**
     * Calculate discount rate as a percentage (0-100)
     * For PERCENT coupons: returns the percentage value
     * For FIXED coupons: converts to percentage based on product price
     */
    public BigDecimal calculateDiscountRate(Coupon coupon, Money productPrice) {
        if (productPrice.isZero()) {
            return BigDecimal.ZERO;
        }

        DiscountValue discountValue = coupon.getDiscountValue();

        if (coupon.isPercentType()) {
            PercentageDiscount percentageDiscount = (PercentageDiscount) discountValue;
            return BigDecimal.valueOf(percentageDiscount.percentage());
        } else {
            // FIXED: calculate as (discount / price) * 100
            Money actualDiscount = coupon.calculateDiscount(productPrice);
            return actualDiscount.amount()
                .divide(productPrice.amount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        }
    }
}
