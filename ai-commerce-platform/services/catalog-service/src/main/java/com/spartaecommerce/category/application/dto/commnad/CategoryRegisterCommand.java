package com.spartaecommerce.category.application.dto.commnad;

public record CategoryRegisterCommand(
    String name,
    String description,
    Long parentCategoryId
) {
}
