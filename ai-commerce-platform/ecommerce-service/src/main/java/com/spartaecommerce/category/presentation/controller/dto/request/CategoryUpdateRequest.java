package com.spartaecommerce.category.presentation.controller.dto.request;

import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;

public record CategoryUpdateRequest(
    String name,
    String description
) {
    public CategoryUpdateCommand toCommand() {
        return new CategoryUpdateCommand(name, description);
    }
}
