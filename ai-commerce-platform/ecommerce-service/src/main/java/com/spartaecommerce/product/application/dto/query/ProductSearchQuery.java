package com.spartaecommerce.product.application.dto.query;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.domain.CustomPageable;

public record ProductSearchQuery(
    Long categoryId,
    Money minPrice,
    Money maxPrice,
    String keyword,
    CustomPageable pageable
) {
    public ProductSearchQuery {
        if (pageable == null) {
            pageable = CustomPageable.ofDefaults();
        }
    }
}