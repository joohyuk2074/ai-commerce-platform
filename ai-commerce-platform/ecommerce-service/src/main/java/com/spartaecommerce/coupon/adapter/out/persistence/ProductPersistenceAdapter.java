package com.spartaecommerce.coupon.adapter.out.persistence;

import com.spartaecommerce.coupon.domain.port.out.LoadProductPort;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 상품 영속성 어댑터
 * Outbound port (LoadProductPort)를 구현합니다
 */
@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements LoadProductPort {

    private final ProductRepository productRepository;

    @Override
    public Product getById(Long productId) {
        return productRepository.getById(productId);
    }
}
