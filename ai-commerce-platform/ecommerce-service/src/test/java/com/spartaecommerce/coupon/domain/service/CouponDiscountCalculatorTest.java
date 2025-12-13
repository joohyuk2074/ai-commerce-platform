package com.spartaecommerce.coupon.domain.service;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.entity.ScopeType;
import com.spartaecommerce.coupon.domain.value.DiscountValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class CouponDiscountCalculatorTest {

    private CouponDiscountCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CouponDiscountCalculator();
    }

    @Test
    @DisplayName("PERCENT 타입 - 할인 금액 계산")
    void calculateDiscountAmount_percent_success() {
        // given
        Coupon coupon = Coupon.createNew(
            "10% 할인 쿠폰",
            DiscountValue.DiscountType.PERCENT,
            BigDecimal.valueOf(10),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money orderAmount = Money.from(BigDecimal.valueOf(50000));

        // when
        Money discountAmount = calculator.calculateDiscountAmount(coupon, orderAmount);

        // then
        assertThat(discountAmount.amount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("PERCENT 타입 - 최대 할인 금액 적용")
    void calculateDiscountAmount_percentWithMaxCap_appliesMaxDiscount() {
        // given
        Coupon coupon = Coupon.createNew(
            "10% 할인 쿠폰 (최대 3000원)",
            DiscountValue.DiscountType.PERCENT,
            BigDecimal.valueOf(10),
            Money.zero(),
            Money.from(BigDecimal.valueOf(3000)),
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money orderAmount = Money.from(BigDecimal.valueOf(50000)); // 10% = 5000원이지만 최대 3000원

        // when
        Money discountAmount = calculator.calculateDiscountAmount(coupon, orderAmount);

        // then
        assertThat(discountAmount.amount()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    @DisplayName("PERCENT 타입 - 최대 할인 금액 미적용 (계산 금액이 더 작음)")
    void calculateDiscountAmount_percentWithMaxCap_doesNotApplyMax() {
        // given
        Coupon coupon = Coupon.createNew(
            "10% 할인 쿠폰 (최대 10000원)",
            DiscountValue.DiscountType.PERCENT,
            BigDecimal.valueOf(10),
            Money.zero(),
            Money.from(BigDecimal.valueOf(10000)),
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money orderAmount = Money.from(BigDecimal.valueOf(50000)); // 10% = 5000원 < 최대 10000원

        // when
        Money discountAmount = calculator.calculateDiscountAmount(coupon, orderAmount);

        // then
        assertThat(discountAmount.amount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("FIXED 타입 - 할인 금액 계산")
    void calculateDiscountAmount_fixed_success() {
        // given
        Coupon coupon = Coupon.createNew(
            "3000원 할인 쿠폰",
            DiscountValue.DiscountType.PERCENT,
            BigDecimal.valueOf(3000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money orderAmount = Money.from(BigDecimal.valueOf(50000));

        // when
        Money discountAmount = calculator.calculateDiscountAmount(coupon, orderAmount);

        // then
        assertThat(discountAmount.amount()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    @DisplayName("FIXED 타입 - 할인 금액이 주문 금액보다 클 때")
    void calculateDiscountAmount_fixedExceedsOrderAmount_returnsOrderAmount() {
        // given
        Coupon coupon = Coupon.createNew(
            "10000원 할인 쿠폰",
            DiscountValue.DiscountType.PERCENT,
            BigDecimal.valueOf(10000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money orderAmount = Money.from(BigDecimal.valueOf(5000));

        // when
        Money discountAmount = calculator.calculateDiscountAmount(coupon, orderAmount);

        // then
        assertThat(discountAmount.amount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("PERCENT 타입 - 할인율 계산")
    void calculateDiscountRate_percent_returnsDiscountValue() {
        // given
        Coupon coupon = Coupon.createNew(
            "15% 할인 쿠폰",
            DiscountValue.DiscountType.PERCENT,
            BigDecimal.valueOf(15),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money productPrice = Money.from(BigDecimal.valueOf(20000));

        // when
        BigDecimal discountRate = calculator.calculateDiscountRate(coupon, productPrice);

        // then
        assertThat(discountRate).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @Test
    @DisplayName("FIXED 타입 - 할인율 계산 (정액을 비율로 환산)")
    void calculateDiscountRate_fixed_convertsToPercentage() {
        // given
        Coupon coupon = Coupon.createNew(
            "5000원 할인 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(5000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money productPrice = Money.from(BigDecimal.valueOf(20000)); // 5000/20000 = 25%

        // when
        BigDecimal discountRate = calculator.calculateDiscountRate(coupon, productPrice);

        // then
        assertThat(discountRate).isEqualByComparingTo(BigDecimal.valueOf(25.00));
    }

    @Test
    @DisplayName("FIXED 타입 - 할인율 계산 (할인액이 가격보다 클 때)")
    void calculateDiscountRate_fixedExceedsPrice_returns100Percent() {
        // given
        Coupon coupon = Coupon.createNew(
            "10000원 할인 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(10000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money productPrice = Money.from(BigDecimal.valueOf(5000)); // 5000/5000 = 100%

        // when
        BigDecimal discountRate = calculator.calculateDiscountRate(coupon, productPrice);

        // then
        assertThat(discountRate).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("가격이 0일 때 할인율은 0")
    void calculateDiscountRate_zeroPrice_returnsZero() {
        // given
        Coupon coupon = Coupon.createNew(
            "5000원 할인 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(5000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money productPrice = Money.zero();

        // when
        BigDecimal discountRate = calculator.calculateDiscountRate(coupon, productPrice);

        // then
        assertThat(discountRate).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("FIXED 타입 - max_discount_amount는 FIXED에서 무시됨")
    void calculateDiscountAmount_fixedWithMaxDiscountAmount_ignoresMax() {
        // given
        Coupon coupon = Coupon.createNew(
            "5000원 할인 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(5000),
            Money.zero(),
            Money.from(BigDecimal.valueOf(1000)), // FIXED에서는 의미 없음
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        Money orderAmount = Money.from(BigDecimal.valueOf(20000));

        // when
        Money discountAmount = calculator.calculateDiscountAmount(coupon, orderAmount);

        // then
        assertThat(discountAmount.amount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }
}
