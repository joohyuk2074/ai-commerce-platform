package com.spartaecommerce.product.presentation.controller.dto.response;

import com.spartaecommerce.product.application.dto.result.ProductResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long productId,
    String name,
    BigDecimal price,
    Integer stock,
    Long categoryId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static ProductResponse from(ProductResult productResult) {
        return new ProductResponse(
            productResult.productId(),
            productResult.name(),
            productResult.price().amount(),
            productResult.stock(),
            productResult.categoryId(),
            productResult.createdAt(),
            productResult.updatedAt()
        );
    }
}
