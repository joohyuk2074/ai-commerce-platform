package com.spartaecommerce.category.adapter.in.web.dto.request;

import com.spartaecommerce.category.application.dto.query.CategorySearchQuery;

public record CategorySearchRequest(
    Long productId
) {
    public CategorySearchQuery toQuery() {
        return new CategorySearchQuery(productId);
    }
}
