package com.spartaecommerce.category.application.service;

import com.spartaecommerce.category.application.dto.query.CategorySearchQuery;
import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.in.CategoryQueryUseCase;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryQueryService implements CategoryQueryUseCase {

    private final LoadCategoryPort loadCategoryPort;
    private final LoadProductPort loadProductPort;

    @Override
    public List<Category> search(CategorySearchQuery searchQuery) {
        Product product = loadProductPort.getById(searchQuery.productId());
        Category category = loadCategoryPort.getById(product.getCategoryId());
        return List.of(category);
    }

    @Override
    public List<CategoryTreeNodeResult> getCategoryTree() {
        List<Category> topLevelCategories = loadCategoryPort.findAllByParentId(null);

        List<CategoryTreeNodeResult> topLevelCategoryNodes = topLevelCategories.stream()
            .map(CategoryTreeNodeResult::from)
            .toList();

        for (CategoryTreeNodeResult categoryTreeNodeResult : topLevelCategoryNodes) {
            List<CategoryTreeNodeResult> children = recursiveSearch(categoryTreeNodeResult.getId());
            categoryTreeNodeResult.addAllChildren(children);
        }

        return topLevelCategoryNodes;
    }

    private List<CategoryTreeNodeResult> recursiveSearch(Long parentCategoryId) {
        List<Category> categories = loadCategoryPort.findAllByParentId(parentCategoryId);

        if (categories.isEmpty()) {
            return Collections.emptyList();
        }

        List<CategoryTreeNodeResult> categoryTreeNodeResults = categories.stream()
            .map(CategoryTreeNodeResult::from)
            .toList();

        for (CategoryTreeNodeResult categoryTreeNodeResult : categoryTreeNodeResults) {
            List<CategoryTreeNodeResult> children = recursiveSearch(categoryTreeNodeResult.getId());
            categoryTreeNodeResult.addAllChildren(children);
        }

        return categoryTreeNodeResults;
    }
}
