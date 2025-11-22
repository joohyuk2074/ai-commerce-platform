package com.spartaecommerce.category.application.dto.commnad;

public record CategoryUpdateCommand(
    String name,
    String description
) {
}
