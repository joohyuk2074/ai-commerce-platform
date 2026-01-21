package com.spartaecommerce.adapter.out.external.dto;

import com.spartaecommerce.common.domain.category.Category;

import java.util.ArrayList;

public record CategoryDto(
    Long categoryId,
    String name,
    String description
) {
    public Category toDomain() {
        return Category.builder()
            .categoryId(categoryId)
            .name(name)
            .description(description)
            .childrenCategoryIds(new ArrayList<>())
            .build();
    }
}
