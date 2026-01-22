package com.spartaecommerce.category.adapter.in.web.internal.dto;

import com.spartaecommerce.category.domain.entity.Category;

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
