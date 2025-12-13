package com.spartaecommerce.product.domain.port.out;

import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.domain.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * 상품 조회 포트 (Outbound Port)
 */
public interface LoadProductPort {

    Optional<Product> findById(Long productId);

    Product getById(Long productId);

    Optional<Product> findByName(String name);

    boolean existsByName(String name);

    boolean existsByCategoryId(Long categoryId);

    Page<Product> search(ProductSearchQuery searchQuery);

    List<Product> findAllByProductIdIn(List<Long> productIds);
}
