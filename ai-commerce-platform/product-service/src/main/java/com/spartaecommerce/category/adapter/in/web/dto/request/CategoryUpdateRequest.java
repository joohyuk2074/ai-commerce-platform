package com.spartaecommerce.category.adapter.in.web.dto.request;

import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;

public record CategoryUpdateRequest(
    String name,
    String description
) {
    public CategoryUpdateCommand toCommand(Long categoryId) {
        return new CategoryUpdateCommand(categoryId, name, description);
    }
}
