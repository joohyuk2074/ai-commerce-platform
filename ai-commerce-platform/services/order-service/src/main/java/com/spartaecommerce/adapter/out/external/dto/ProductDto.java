package com.spartaecommerce.adapter.out.external.dto;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.domain.product.Product;

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
    public Product toDomain() {
        return Product.builder()
            .productId(productId)
            .name(name)
            .description(description)
            .price(Money.from(price))
            .stock(stock)
            .categoryId(categoryId)
            .isOrderable(isOrderable)
            .build();
    }
}
