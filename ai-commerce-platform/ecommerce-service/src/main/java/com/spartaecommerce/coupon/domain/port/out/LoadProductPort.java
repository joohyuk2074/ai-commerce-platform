package com.spartaecommerce.coupon.domain.port.out;

import com.spartaecommerce.product.domain.entity.Product;

/**
 * 상품 조회 포트 (Outbound Port)
 */
public interface LoadProductPort {

    /**
     * ID로 상품을 조회합니다 (없으면 예외 발생)
     */
    Product getById(Long productId);
}
