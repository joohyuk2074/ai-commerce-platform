package com.spartaecommerce.category.adapter.in.web.dto.response;

import com.spartaecommerce.common.domain.category.Category;

public record CategoryResponse(
    Long categoryId,
    String name,
    String description
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getCategoryId(), category.getName(), category.getDescription());
    }
}
