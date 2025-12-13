package com.spartaecommerce.coupon.domain.value;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;

import java.time.LocalDateTime;

/**
 * 쿠폰 유효 기간을 나타내는 Value Object
 * 쿠폰이 사용 가능한 날짜 범위를 정의합니다.
 */
public record ValidityPeriod(
    LocalDateTime startDate,
    LocalDateTime endDate
) {

    public ValidityPeriod {
        validate(startDate, endDate);
    }

    /**
     * 유효 기간을 생성합니다
     */
    public static ValidityPeriod of(LocalDateTime startDate, LocalDateTime endDate) {
        return new ValidityPeriod(startDate, endDate);
    }

    /**
     * 주어진 시각이 유효 기간 내인지 확인합니다
     *
     * @param now 확인할 시각
     * @return 유효 기간 내이면 true
     */
    public boolean isActiveAt(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Now must not be null");
        }
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    /**
     * 유효 기간이 아직 시작되지 않았는지 확인합니다
     *
     * @param now 확인할 시각
     * @return 시작 전이면 true
     */
    public boolean isBeforeStart(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Now must not be null");
        }
        return now.isBefore(startDate);
    }

    /**
     * 유효 기간이 이미 종료되었는지 확인합니다
     *
     * @param now 확인할 시각
     * @return 종료되었으면 true
     */
    public boolean isExpired(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Now must not be null");
        }
        return now.isAfter(endDate);
    }

    private static void validate(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Start date and end date must not be null"
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Start date must be before or equal to end date"
            );
        }
    }
}
