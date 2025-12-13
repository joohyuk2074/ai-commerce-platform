package com.spartaecommerce.coupon.domain.entity;

/**
 * 쿠폰 발급 상태를 나타내는 열거형
 */
public enum CouponStatus {
    /**
     * 미발급 - 쿠폰이 생성되었지만 아직 사용자에게 발급되지 않은 상태
     */
    PENDING("미발급"),

    /**
     * 발급됨 - 사용자에게 발급되었지만 아직 사용되지 않은 상태
     */
    ISSUED("발급됨"),

    /**
     * 사용됨 - 사용자가 쿠폰을 사용한 상태
     */
    USED("사용됨"),

    /**
     * 만료됨 - 쿠폰이 만료된 상태
     */
    EXPIRED("만료됨");

    private final String description;

    CouponStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 발급 가능한 상태인지 확인
     */
    public boolean canBeIssued() {
        return this == PENDING;
    }

    /**
     * 사용 가능한 상태인지 확인
     */
    public boolean canBeUsed() {
        return this == ISSUED;
    }

    /**
     * 만료 가능한 상태인지 확인
     */
    public boolean canBeExpired() {
        return this == ISSUED;
    }
}
