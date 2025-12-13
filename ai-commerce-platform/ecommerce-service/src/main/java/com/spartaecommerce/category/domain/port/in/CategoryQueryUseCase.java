package com.spartaecommerce.category.domain.port.in;

import com.spartaecommerce.category.application.dto.query.CategorySearchQuery;
import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;
import com.spartaecommerce.category.domain.entity.Category;

import java.util.List;

public interface CategoryQueryUseCase {

    List<Category> search(CategorySearchQuery searchQuery);

    List<CategoryTreeNodeResult> getCategoryTree();
}
