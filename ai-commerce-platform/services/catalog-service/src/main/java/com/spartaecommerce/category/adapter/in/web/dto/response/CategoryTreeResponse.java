package com.spartaecommerce.category.adapter.in.web.dto.response;

import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;

import java.util.List;

public record CategoryTreeResponse(
    List<CategoryTreeNodeResult> categories
) {
}
