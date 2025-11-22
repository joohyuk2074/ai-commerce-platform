package com.spartaecommerce.category.presentation.controller.dto.response;

import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;

import java.util.List;

public record CategoryTreeResponse(
    List<CategoryTreeNodeResult> categoryTreeNodeResults
) {
}
