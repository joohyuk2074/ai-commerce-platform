package com.spartaecommerce.coupon.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.coupon.domain.value.DiscountValue;
import com.spartaecommerce.coupon.domain.value.PercentageDiscount;
import com.spartaecommerce.coupon.domain.value.ValidityPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @Test
    @DisplayName("쿠폰 생성 - 정상")
    void createCoupon_success() {
        // given
        String couponName = "신규 회원 할인";
        Money minOrderAmount = Money.from(BigDecimal.valueOf(10000));
        Money maxDiscountAmount = Money.from(BigDecimal.valueOf(5000));
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(30);
        Integer usageLimit = 100;

        // when
        Coupon coupon = Coupon.createNew(
            couponName,
            DiscountValue.DiscountType.PERCENT,
            BigDecimal.valueOf(10),
            minOrderAmount,
            maxDiscountAmount,
            ScopeType.ALL,
            null,
            startDate,
            endDate,
            usageLimit
        );

        // then
        assertThat(coupon).isNotNull();
        assertThat(coupon.getCouponName()).isEqualTo(couponName);
        assertThat(coupon.isPercentType()).isTrue();
        assertThat(coupon.isDeleted()).isFalse();
        assertThat(coupon.getUsedCount()).isZero();
        assertThat(coupon.getIssuedCount()).isZero();
    }

    @Test
    @DisplayName("PERCENT 타입 쿠폰 - 할인율이 100을 초과하면 예외")
    void createCoupon_percentOver100_throwsException() {
        // when & then
        assertThatThrownBy(() -> PercentageDiscount.of(101))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("must be between");
    }

    @Test
    @DisplayName("PERCENT 타입 쿠폰 - 할인율이 1 미만이면 예외")
    void createCoupon_percentLessThan1_throwsException() {
        // when & then
        assertThatThrownBy(() -> PercentageDiscount.of(0))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("must be between");
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 예외")
    void createCoupon_startDateAfterEndDate_throwsException() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.minusDays(1);

        // when & then
        assertThatThrownBy(() -> ValidityPeriod.of(startDate, endDate))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Start date must be before or equal to end date");
    }

    @Test
    @DisplayName("쿠폰 활성 여부 확인 - 활성 기간 내, 사용 가능 횟수 남음")
    void isActive_withinDateAndUsageAvailable_returnsTrue() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            now.minusDays(1),
            now.plusDays(1),
            10
        );

        // when
        boolean isActive = coupon.isActive(now);

        // then
        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("쿠폰 활성 여부 확인 - 시작일 이전")
    void isActive_beforeStartDate_returnsFalse() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            now.plusDays(1),
            now.plusDays(2),
            10
        );

        // when
        boolean isActive = coupon.isActive(now);

        // then
        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("쿠폰 활성 여부 확인 - 종료일 이후")
    void isActive_afterEndDate_returnsFalse() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            now.minusDays(2),
            now.minusDays(1),
            10
        );

        // when
        boolean isActive = coupon.isActive(now);

        // then
        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("쿠폰 활성 여부 확인 - 사용 횟수 소진")
    void isActive_usageLimitExhausted_returnsFalse() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            now.minusDays(1),
            now.plusDays(1),
            2
        );

        coupon.incrementUsedCount();
        coupon.incrementUsedCount();

        // when
        boolean isActive = coupon.isActive(now);

        // then
        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("쿠폰 활성 여부 확인 - 논리 삭제된 쿠폰")
    void isActive_deletedCoupon_returnsFalse() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            now.minusDays(1),
            now.plusDays(1),
            10
        );

        coupon.delete(LocalDateTime.now());

        // when
        boolean isActive = coupon.isActive(now);

        // then
        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("주문에 적용 가능 여부 - 최소 주문 금액 충족")
    void canApplyToOrder_meetsMinOrderAmount_returnsTrue() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Money minOrderAmount = Money.from(BigDecimal.valueOf(10000));
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            minOrderAmount,
            null,
            ScopeType.ALL,
            null,
            now.minusDays(1),
            now.plusDays(1),
            10
        );

        Money orderAmount = Money.from(BigDecimal.valueOf(15000));

        // when
        boolean canApply = coupon.canApplyToOrder(orderAmount, now);

        // then
        assertThat(canApply).isTrue();
    }

    @Test
    @DisplayName("주문에 적용 가능 여부 - 최소 주문 금액 미충족")
    void canApplyToOrder_doesNotMeetMinOrderAmount_returnsFalse() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Money minOrderAmount = Money.from(BigDecimal.valueOf(10000));
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            minOrderAmount,
            null,
            ScopeType.ALL,
            null,
            now.minusDays(1),
            now.plusDays(1),
            10
        );

        Money orderAmount = Money.from(BigDecimal.valueOf(5000));

        // when
        boolean canApply = coupon.canApplyToOrder(orderAmount, now);

        // then
        assertThat(canApply).isFalse();
    }

    @Test
    @DisplayName("쿠폰 사용 횟수 증가 - 정상")
    void incrementUsedCount_success() {
        // given
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        // when
        coupon.incrementUsedCount();

        // then
        assertThat(coupon.getUsedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("쿠폰 사용 횟수 증가 - 사용 제한 초과 시 예외")
    void incrementUsedCount_exceedsLimit_throwsException() {
        // given
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            1
        );

        coupon.incrementUsedCount();

        // when & then
        assertThatThrownBy(coupon::incrementUsedCount)
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("usage limit exceeded");
    }

    @Test
    @DisplayName("쿠폰 삭제 - 정상")
    void delete_success() {
        // given
        Coupon coupon = Coupon.createNew(
            "테스트 쿠폰",
            DiscountValue.DiscountType.FIXED,
            BigDecimal.valueOf(1000),
            Money.zero(),
            null,
            ScopeType.ALL,
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        );

        // when
        coupon.delete(LocalDateTime.now());

        // then
        assertThat(coupon.isDeleted()).isTrue();
        assertThat(coupon.getDeletedAt()).isNotNull();
    }
}
