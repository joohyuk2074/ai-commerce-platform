package com.spartaecommerce.product.adapter.in.web.dto.request;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.domain.CustomPageable;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;

import java.math.BigDecimal;

public record ProductSearchRequest(
    Long categoryId,
    Boolean isOrderable,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    String keyword,

    Integer page,
    Integer size,
    String sortBy,
    String direction,
    Long lastProductId
) {

    public ProductSearchQuery toQuery() {
        CustomPageable customPageable = CustomPageable.of(page, size, sortBy, direction, lastProductId);

        return new ProductSearchQuery(
            categoryId,
            isOrderable,
            Money.from(minPrice),
            Money.from(maxPrice),
            keyword,
            customPageable
        );
    }
}
