package com.spartaecommerce.product.application.dto.result;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.domain.entity.Product;

import java.time.LocalDateTime;

public record ProductResult(
    Long productId,
    String name,
    String description,
    Money price,
    Integer stock,
    Long categoryId,
    boolean isOrderable,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static ProductResult from(Product product) {
        return new ProductResult(
            product.getProductId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategoryId(),
            product.isOrderable(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
