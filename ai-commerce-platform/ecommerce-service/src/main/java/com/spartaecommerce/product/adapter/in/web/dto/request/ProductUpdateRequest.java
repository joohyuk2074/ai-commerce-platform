package com.spartaecommerce.product.adapter.in.web.dto.request;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;

import java.math.BigDecimal;

public record ProductUpdateRequest(
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    Long categoryId
) {
    public ProductUpdateCommand toCommand(Long productId) {
        return new ProductUpdateCommand(
            productId,
            name,
            description,
            Money.from(price),
            stock,
            categoryId
        );
    }
}
