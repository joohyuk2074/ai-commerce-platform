package com.spartaecommerce.product.domain.repository;

import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Long save(Product product);

    void saveAll(List<Product> products);

    boolean existsByName(String name);

    boolean existsByCategoryId(Long categoryId);

    Optional<Product> findByName(String name);

    Product getById(Long productId);

    Page<Product> search(ProductSearchQuery searchQuery);

    List<Product> findAllByProductIdIn(List<Long> productIds);
}
