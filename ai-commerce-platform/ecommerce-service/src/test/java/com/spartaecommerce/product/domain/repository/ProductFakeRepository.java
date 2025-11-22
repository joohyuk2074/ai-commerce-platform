package com.spartaecommerce.product.domain.repository;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ProductFakeRepository implements ProductRepository {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(Product product) {
        if (product.getProductId() == null) {
            long productId = idGenerator.getAndIncrement();
            Product newProduct = Product.builder()
                .productId(productId)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .deleted(false)
                .categoryId(product.getCategoryId())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
            products.put(productId, newProduct);
            return productId;
        } else {
            products.put(product.getProductId(), product);
            return product.getProductId();
        }
    }

    @Override
    public void saveAll(List<Product> productList) {
        for (Product product : productList) {
            save(product);
        }
    }

    @Override
    public boolean existsByName(String name) {
        return false;
    }

    @Override
    public Optional<Product> findByName(String name) {
        return Optional.empty();
    }

    @Override
    public Product getById(Long productId) {
        Product product = products.get(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "productId: " + productId);
        }
        return product;
    }

    public List<Product> findAllByIdIn(List<Long> productIds) {
        return List.of();
    }

    @Override
    public List<Product> findAllByProductIdIn(List<Long> productIds) {
        return products.values().stream()
            .filter(product -> productIds.contains(product.getProductId()))
            .toList();
    }

    @Override
    public Page<Product> search(ProductSearchQuery searchQuery) {
        return null;
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return false;
    }

    public void clear() {
        products.clear();
    }
}