package com.spartaecommerce.category.domain.port.in;

import com.spartaecommerce.category.application.dto.query.CategorySearchQuery;
import com.spartaecommerce.category.application.dto.query.TopSellingProductsQuery;
import com.spartaecommerce.category.application.dto.result.CategoryProductSalesRankingResult;
import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;
import com.spartaecommerce.common.domain.category.Category;

import java.util.List;

public interface CategoryQueryUseCase {

    List<Category> search(CategorySearchQuery searchQuery);

    List<CategoryTreeNodeResult> getCategoryTree();

    List<CategoryProductSalesRankingResult> getTopSellingProductsByCategory(TopSellingProductsQuery query);
}
