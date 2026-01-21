package com.spartaecommerce.coupon.adapter.out.persistence;

import com.spartaecommerce.coupon.domain.port.out.LoadProductPort;
import com.spartaecommerce.common.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 상품 영속성 어댑터
 * Outbound port (LoadProductPort)를 구현합니다
 */
@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements LoadProductPort {

    private final com.spartaecommerce.product.domain.port.out.LoadProductPort loadProductPort;

    @Override
    public Product getById(Long productId) {
        return loadProductPort.getById(productId);
    }
}
