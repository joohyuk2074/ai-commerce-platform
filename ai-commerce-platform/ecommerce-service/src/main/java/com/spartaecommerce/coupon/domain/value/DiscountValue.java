package com.spartaecommerce.coupon.domain.value;

import com.spartaecommerce.common.domain.Money;

/**
 * 할인 값을 나타내는 Value Object
 * 할인 타입에 따라 다른 구현체를 사용합니다.
 */
public interface DiscountValue {

    /**
     * 주어진 금액에 대한 할인액을 계산합니다
     *
     * @param orderAmount 할인 대상 금액
     * @param maxDiscountAmount 최대 할인 금액 (null 가능)
     * @return 계산된 할인액
     */
    Money calculateDiscount(Money orderAmount, Money maxDiscountAmount);

    /**
     * 할인 타입을 반환합니다
     *
     * @return 할인 타입
     */
    DiscountType getType();

    /**
     * 값의 유효성을 검증합니다
     */
    void validate();

    /**
     * 저장을 위한 문자열 표현을 반환합니다
     * 형식: "type:value" (예: "PERCENT:10", "FIXED:10000")
     *
     * @return 문자열 표현
     */
    String toStorageString();

    /**
     * 문자열 표현으로부터 DiscountValue를 생성합니다
     *
     * @param storageString 저장 형식 문자열
     * @return DiscountValue 인스턴스
     */
    static DiscountValue fromStorageString(String storageString) {
        if (storageString == null || storageString.isBlank()) {
            throw new IllegalArgumentException("Storage string must not be null or blank");
        }

        String[] parts = storageString.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid storage string format: " + storageString);
        }

        String type = parts[0];
        String value = parts[1];

        return switch (type) {
            case "PERCENT" -> PercentageDiscount.of(Integer.parseInt(value));
            case "FIXED" -> FixedDiscount.of(Money.from(new java.math.BigDecimal(value)));
            default -> throw new IllegalArgumentException("Unknown discount type: " + type);
        };
    }

    enum DiscountType {
        PERCENT, FIXED
    }
}
