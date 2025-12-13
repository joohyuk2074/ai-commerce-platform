package com.spartaecommerce.coupon.domain.port.in;

import com.spartaecommerce.common.domain.CustomPageable;
import com.spartaecommerce.coupon.application.dto.result.CouponResult;
import org.springframework.data.domain.Page;

/**
 * 쿠폰 조회 유스케이스
 */
public interface CouponQueryUseCase {

    /**
     * ID로 쿠폰을 조회합니다
     *
     * @param couponId 쿠폰 ID
     * @return 쿠폰 정보
     */
    CouponResult getById(Long couponId);

    /**
     * 쿠폰 목록을 검색합니다
     *
     * @param isActive 활성 상태 필터 (null이면 전체)
     * @param pageable 페이징 정보
     * @return 쿠폰 목록
     */
    Page<CouponResult> search(Boolean isActive, CustomPageable pageable);
}
