package com.spartaecommerce.coupon.domain.port.in;

import com.spartaecommerce.coupon.application.dto.result.MaxDiscountResult;

/**
 * 상품에 대한 최대 할인 조회 유스케이스
 */
public interface GetMaxDiscountForProductUseCase {

    /**
     * 상품에 적용 가능한 최대 할인을 조회합니다
     *
     * @param productId 상품 ID
     * @return 최대 할인 정보
     */
    MaxDiscountResult getMaxDiscount(Long productId);
}
