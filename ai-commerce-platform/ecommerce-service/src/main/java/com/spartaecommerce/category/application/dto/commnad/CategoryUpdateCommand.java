package com.spartaecommerce.category.application.dto.commnad;

public record CategoryUpdateCommand(
    Long categoryId,
    String name,
    String description
) {
}
