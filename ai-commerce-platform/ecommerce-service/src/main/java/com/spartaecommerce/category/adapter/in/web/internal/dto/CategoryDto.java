package com.spartaecommerce.category.adapter.in.web.internal.dto;

import com.spartaecommerce.common.domain.category.Category;

public record CategoryDto(
    Long categoryId,
    String name,
    String description
) {
    public static CategoryDto from(Category category) {
        return new CategoryDto(
            category.getCategoryId(),
            category.getName(),
            category.getDescription()
        );
    }
}
