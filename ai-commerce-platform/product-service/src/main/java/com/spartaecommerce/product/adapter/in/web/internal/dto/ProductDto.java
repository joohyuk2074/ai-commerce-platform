package com.spartaecommerce.product.adapter.in.web.internal.dto;

import com.spartaecommerce.product.domain.entity.Product;

import java.math.BigDecimal;

public record ProductDto(
    Long productId,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    Long categoryId,
    boolean isOrderable
) {
    public static ProductDto from(Product product) {
        return new ProductDto(
            product.getProductId(),
            product.getName(),
            product.getDescription(),
            product.getPrice().amount(),
            product.getStock(),
            product.getCategoryId(),
            product.isOrderable()
        );
    }
}
