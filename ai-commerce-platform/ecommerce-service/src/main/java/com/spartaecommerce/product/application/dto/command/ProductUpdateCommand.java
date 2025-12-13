package com.spartaecommerce.product.application.dto.command;

import com.spartaecommerce.common.domain.Money;

public record ProductUpdateCommand(
    Long productId,
    String name,
    String description,
    Money price,
    Integer stock,
    Long categoryId
) {
}