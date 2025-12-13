package com.spartaecommerce.coupon.domain.entity;

import com.spartaecommerce.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CouponUserTest {

    @Test
    @DisplayName("쿠폰 사용자 생성 - 정상")
    void createCouponUser_success() {
        // given
        Long couponId = 1L;
        String code = "TEST-CODE-1234";

        // when
        CouponUser couponUser = CouponUser.createNew(couponId, code);

        // then
        assertThat(couponUser).isNotNull();
        assertThat(couponUser.getCouponId()).isEqualTo(couponId);
        assertThat(couponUser.getCode()).isEqualTo(code);
        assertThat(couponUser.getStatus()).isEqualTo(CouponStatus.PENDING);
        assertThat(couponUser.getUserId()).isNull();
        assertThat(couponUser.getCreatedAt()).isNotNull();
        assertThat(couponUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("쿠폰 사용자 생성 - 쿠폰 ID가 null이면 예외")
    void createCouponUser_nullCouponId_throwsException() {
        // when & then
        assertThatThrownBy(() -> CouponUser.createNew(null, "TEST-CODE"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("쿠폰 ID는 필수입니다");
    }

    @Test
    @DisplayName("쿠폰 사용자 생성 - 쿠폰 코드가 null이면 예외")
    void createCouponUser_nullCode_throwsException() {
        // when & then
        assertThatThrownBy(() -> CouponUser.createNew(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("쿠폰 코드는 필수입니다");
    }

    @Test
    @DisplayName("쿠폰 사용자 생성 - 쿠폰 코드가 너무 짧으면 예외")
    void createCouponUser_codeTooShort_throwsException() {
        // when & then
        assertThatThrownBy(() -> CouponUser.createNew(1L, "SHORT"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("8자 이상 20자 이하");
    }

    @Test
    @DisplayName("쿠폰 사용자 생성 - 쿠폰 코드가 너무 길면 예외")
    void createCouponUser_codeTooLong_throwsException() {
        // when & then
        assertThatThrownBy(() -> CouponUser.createNew(1L, "A".repeat(21)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("8자 이상 20자 이하");
    }

    @Test
    @DisplayName("쿠폰 발급 - 정상")
    void issueCoupon_success() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");
        Long userId = 100L;

        // when
        couponUser.issue(userId);

        // then
        assertThat(couponUser.getUserId()).isEqualTo(userId);
        assertThat(couponUser.getStatus()).isEqualTo(CouponStatus.ISSUED);
    }

    @Test
    @DisplayName("쿠폰 발급 - 사용자 ID가 null이면 예외")
    void issueCoupon_nullUserId_throwsException() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");

        // when & then
        assertThatThrownBy(() -> couponUser.issue(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용자 ID는 필수입니다");
    }

    @Test
    @DisplayName("쿠폰 발급 - 이미 발급된 쿠폰이면 예외")
    void issueCoupon_alreadyIssued_throwsException() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");
        couponUser.issue(100L);

        // when & then
        assertThatThrownBy(() -> couponUser.issue(200L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("발급 불가능한 쿠폰입니다");
    }

    @Test
    @DisplayName("쿠폰 사용 - 정상")
    void useCoupon_success() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");
        couponUser.issue(100L);

        // when
        couponUser.use();

        // then
        assertThat(couponUser.getStatus()).isEqualTo(CouponStatus.USED);
    }

    @Test
    @DisplayName("쿠폰 사용 - 미발급 상태에서 사용 시도하면 예외")
    void useCoupon_pending_throwsException() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");

        // when & then
        assertThatThrownBy(() -> couponUser.use())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용 불가능한 쿠폰입니다");
    }

    @Test
    @DisplayName("쿠폰 만료 - 정상")
    void expireCoupon_success() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");
        couponUser.issue(100L);

        // when
        couponUser.expire();

        // then
        assertThat(couponUser.getStatus()).isEqualTo(CouponStatus.EXPIRED);
    }

    @Test
    @DisplayName("쿠폰 만료 - 미발급 상태에서 만료 시도하면 예외")
    void expireCoupon_pending_throwsException() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");

        // when & then
        assertThatThrownBy(() -> couponUser.expire())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("만료 불가능한 쿠폰입니다");
    }

    @Test
    @DisplayName("발급 가능 여부 확인 - PENDING 상태")
    void canBeIssued_pending() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");

        // when & then
        assertThat(couponUser.canBeIssued()).isTrue();
    }

    @Test
    @DisplayName("발급 가능 여부 확인 - ISSUED 상태")
    void canBeIssued_issued() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");
        couponUser.issue(100L);

        // when & then
        assertThat(couponUser.canBeIssued()).isFalse();
    }

    @Test
    @DisplayName("사용 가능 여부 확인 - ISSUED 상태")
    void canBeUsed_issued() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");
        couponUser.issue(100L);

        // when & then
        assertThat(couponUser.canBeUsed()).isTrue();
    }

    @Test
    @DisplayName("사용 가능 여부 확인 - USED 상태")
    void canBeUsed_used() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");
        couponUser.issue(100L);
        couponUser.use();

        // when & then
        assertThat(couponUser.canBeUsed()).isFalse();
    }

    @Test
    @DisplayName("사용자 소유 여부 확인 - 동일한 사용자")
    void isOwnedBy_sameUser() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");
        couponUser.issue(100L);

        // when & then
        assertThat(couponUser.isOwnedBy(100L)).isTrue();
    }

    @Test
    @DisplayName("사용자 소유 여부 확인 - 다른 사용자")
    void isOwnedBy_differentUser() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");
        couponUser.issue(100L);

        // when & then
        assertThat(couponUser.isOwnedBy(200L)).isFalse();
    }

    @Test
    @DisplayName("사용자 소유 여부 확인 - 미발급 상태")
    void isOwnedBy_pending() {
        // given
        CouponUser couponUser = CouponUser.createNew(1L, "TEST-CODE-1234");

        // when & then
        assertThat(couponUser.isOwnedBy(100L)).isFalse();
    }
}
