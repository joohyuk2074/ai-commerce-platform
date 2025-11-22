package com.spartaecommerce.category.application;

import com.spartaecommerce.category.application.dto.commnad.CategoryRegisterCommand;
import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;
import com.spartaecommerce.category.application.dto.query.CategorySearchQuery;
import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.repository.CategoryRepository;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Long register(CategoryRegisterCommand registerCommand) {
        if (categoryRepository.existsByName(registerCommand.name())) {
            throw new BusinessException(ErrorCode.ENTITY_ALREADY_EXISTS, "Category name: " + registerCommand.name());
        }

        Category category = Category.createNew(
            registerCommand.name(),
            registerCommand.description(),
            registerCommand.parentCategoryId()
        );

        return categoryRepository.save(category);
    }

    @Transactional
    public void update(Long categoryId, CategoryUpdateCommand updateCommand) {
        Category category = categoryRepository.getById(categoryId);
        category.update(updateCommand.name(), updateCommand.description());
        categoryRepository.save(category);
    }

    public List<Category> search(CategorySearchQuery searchQuery) {
        Product product = productRepository.getById(searchQuery.productId());
        Category category = categoryRepository.getById(product.getCategoryId());
        return List.of(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        Category category = categoryRepository.getById(categoryId);

        if (category.isDeleted()) {
            throw new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Already deleted category: " + categoryId
            );
        }

        if (categoryRepository.hasActiveChildren(categoryId)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot delete category with active children: " + categoryId
            );
        }

        if (productRepository.existsByCategoryId(categoryId)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot delete category with active products: " + categoryId
            );
        }

        category.delete();
        categoryRepository.save(category);
    }

    public List<CategoryTreeNodeResult> getCategoryTree() {
        List<Category> topLevelCategories = categoryRepository.findAllByParentId(null);

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
        List<Category> categories = categoryRepository.findAllByParentId(parentCategoryId);

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
