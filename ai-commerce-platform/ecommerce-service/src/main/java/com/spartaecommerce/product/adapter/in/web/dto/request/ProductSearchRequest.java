package com.spartaecommerce.product.adapter.in.web.dto.request;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    String direction
) {

    public ProductSearchQuery toQuery() {
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (size != null && size > 0 && size <= 100) ? size : 20;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        String sortDirection = (direction != null && !direction.isBlank()) ? direction : "DESC";

        Sort.Direction dir = sortDirection.equalsIgnoreCase("ASC")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(dir, sortField));

        return new ProductSearchQuery(
            categoryId,
            isOrderable,
            Money.from(minPrice),
            Money.from(maxPrice),
            keyword,
            pageable
        );
    }
}
