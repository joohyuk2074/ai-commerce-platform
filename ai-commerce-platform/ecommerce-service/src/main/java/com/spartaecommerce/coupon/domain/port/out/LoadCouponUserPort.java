package com.spartaecommerce.coupon.domain.port.out;

import com.spartaecommerce.coupon.domain.entity.CouponUser;

import java.util.Optional;
import java.util.Set;

/**
 * 쿠폰 사용자 조회 포트 (Outbound Port)
 */
public interface LoadCouponUserPort {

    /**
     * ID로 쿠폰 사용자를 조회합니다
     */
    Optional<CouponUser> findById(Long couponUserId);

    /**
     * ID로 쿠폰 사용자를 조회합니다 (없으면 예외 발생)
     */
    CouponUser getById(Long couponUserId);

    /**
     * 쿠폰 코드로 조회합니다
     */
    Optional<CouponUser> findByCode(String code);

    /**
     * 쿠폰 코드로 조회합니다 (없으면 예외 발생)
     */
    CouponUser getByCode(String code);

    /**
     * 쿠폰 코드 존재 여부를 확인합니다
     */
    boolean existsByCode(String code);

    /**
     * 주어진 쿠폰 코드 목록 중 이미 존재하는 코드들을 조회합니다 (배치 조회)
     * N+1 문제를 해결하기 위한 메서드
     *
     * @param codes 확인할 쿠폰 코드 목록
     * @return 이미 존재하는 코드들의 집합
     */
    Set<String> findExistingCodes(Set<String> codes);

    /**
     * 특정 쿠폰의 발행된 개수를 조회합니다
     */
    long countByCouponId(Long couponId);
}
