package com.spartaecommerce.product.presentation.controller.dto.request;

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
    public ProductUpdateCommand toCommand() {
        return new ProductUpdateCommand(
            name,
            description,
            Money.from(price),
            stock,
            categoryId
        );
    }
}
