package com.spartaecommerce.coupon.domain.entity;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 쿠폰 도메인 엔티티
 * 실제 발행된 쿠폰을 나타내며, 오프라인 쿠폰 코드와 사용자 정보를 관리합니다.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class CouponUser {

    private Long couponUserId;
    private Long userId;
    private Long couponId;
    private String code;
    private CouponStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version; // Optimistic lock

    /**
     * 새로운 쿠폰을 생성합니다 (미발급 상태)
     *
     * @param couponId 쿠폰 ID
     * @param code 쿠폰 코드
     * @return 생성된 쿠폰
     */
    public static CouponUser createNew(Long couponId, String code) {
        validateCouponId(couponId);
        validateCode(code);

        LocalDateTime now = LocalDateTime.now();

        return CouponUser.builder()
                .couponId(couponId)
                .code(code)
                .status(CouponStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 기존 데이터로부터 쿠폰 사용자 엔티티를 재구성합니다 (for JPA)
     */
    public static CouponUser reconstitute(
            Long couponUserId,
            Long userId,
            Long couponId,
            String code,
            CouponStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long version
    ) {
        return new CouponUser(couponUserId, userId, couponId, code, status, createdAt, updatedAt, version);
    }

    /**
     * 사용자에게 쿠폰을 발급합니다
     *
     * @param userId 사용자 ID
     * @throws BusinessException 발급 불가능한 상태인 경우
     */
    public void issue(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용자 ID는 필수입니다.");
        }

        if (!status.canBeIssued()) {
            throw new BusinessException(ErrorCode.INVALID_COUPON_STATUS,
                    "발급 불가능한 쿠폰입니다. 현재 상태: " + status.getDescription());
        }

        this.userId = userId;
        this.status = CouponStatus.ISSUED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰을 사용합니다
     *
     * @throws BusinessException 사용 불가능한 상태인 경우
     */
    public void use() {
        if (!status.canBeUsed()) {
            throw new BusinessException(ErrorCode.INVALID_COUPON_STATUS,
                    "사용 불가능한 쿠폰입니다. 현재 상태: " + status.getDescription());
        }

        this.status = CouponStatus.USED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰을 만료시킵니다
     *
     * @throws BusinessException 만료 불가능한 상태인 경우
     */
    public void expire() {
        if (!status.canBeExpired()) {
            throw new BusinessException(ErrorCode.INVALID_COUPON_STATUS,
                    "만료 불가능한 쿠폰입니다. 현재 상태: " + status.getDescription());
        }

        this.status = CouponStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 발급 가능한 쿠폰인지 확인
     */
    public boolean canBeIssued() {
        return status.canBeIssued();
    }

    /**
     * 사용 가능한 쿠폰인지 확인
     */
    public boolean canBeUsed() {
        return status.canBeUsed();
    }

    /**
     * 특정 사용자의 쿠폰인지 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    private static void validateCouponId(Long couponId) {
        if (couponId == null || couponId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "쿠폰 ID는 필수입니다.");
        }
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "쿠폰 코드는 필수입니다.");
        }
        if (code.length() < 8 || code.length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "쿠폰 코드는 8자 이상 20자 이하여야 합니다.");
        }
    }
}
