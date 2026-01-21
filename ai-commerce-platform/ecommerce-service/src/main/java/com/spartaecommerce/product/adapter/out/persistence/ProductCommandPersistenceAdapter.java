package com.spartaecommerce.product.adapter.out.persistence;

import com.spartaecommerce.product.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import com.spartaecommerce.product.adapter.out.persistence.jpa.repository.ProductJpaRepository;
import com.spartaecommerce.common.domain.product.Product;
import com.spartaecommerce.product.domain.port.out.SaveProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductCommandPersistenceAdapter implements SaveProductPort {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Long save(Product product) {
        ProductJpaEntity productJpaEntity = ProductJpaEntity.from(product);
        return productJpaRepository.save(productJpaEntity).getProductId();
    }

    @Override
    public void saveAll(List<Product> products) {
        List<ProductJpaEntity> productJpaEntities = products.stream()
            .map(ProductJpaEntity::from)
            .toList();

        productJpaRepository.saveAll(productJpaEntities);
    }
}
