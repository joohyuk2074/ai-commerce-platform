package com.spartaecommerce.category.presentation.controller.dto.response;

import com.spartaecommerce.category.domain.entity.Category;

public record CategoryResponse(
    Long categoryId,
    String name,
    String description
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getCategoryId(), category.getName(), category.getDescription());
    }
}
