package com.spartaecommerce.category.presentation.controller.dto.request;

import com.spartaecommerce.category.application.dto.commnad.CategoryRegisterCommand;

public record CategoryRegisterRequest(
    String name,
    String description,
    Long parentCategoryId
) {
    public CategoryRegisterCommand toCommand() {
        return new CategoryRegisterCommand(name, description, parentCategoryId);
    }
}
