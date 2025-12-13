package com.spartaecommerce.category.adapter.in.web.dto.request;

import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;

public record CategoryUpdateRequest(
    String name,
    String description
) {
    public CategoryUpdateCommand toCommand() {
        return new CategoryUpdateCommand(name, description);
    }
}
