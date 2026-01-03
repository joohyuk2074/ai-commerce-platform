package com.spartaecommerce.category.application.service;

import com.spartaecommerce.category.application.dto.commnad.CategoryRegisterCommand;
import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.in.CategoryCommandUseCase;
import com.spartaecommerce.category.domain.port.out.CategoryClosurePort;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.category.domain.port.out.SaveCategoryPort;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryCommandService implements CategoryCommandUseCase {

    private final LoadCategoryPort loadCategoryPort;
    private final SaveCategoryPort saveCategoryPort;
    private final CategoryClosurePort categoryClosurePort;

    @Override
    @CacheEvict(value = "categoryTree", allEntries = true)
    public Long register(CategoryRegisterCommand registerCommand) {
        if (loadCategoryPort.existsByName(registerCommand.name())) {
            throw new BusinessException(
                ErrorCode.ENTITY_ALREADY_EXISTS,
                "Category name: " + registerCommand.name());
        }

        // 부모 카테고리 존재 여부 검증
        if (registerCommand.parentCategoryId() != null
            && !loadCategoryPort.existsById(registerCommand.parentCategoryId())) {
            throw new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Parent category not found: " + registerCommand.parentCategoryId());
        }

        Category category = Category.createNew(
            registerCommand.name(),
            registerCommand.description(),
            registerCommand.parentCategoryId()
        );

        Long categoryId = saveCategoryPort.save(category);

        // Closure Table 엔트리 생성
        categoryClosurePort.insertClosureForNewCategory(
            categoryId,
            registerCommand.parentCategoryId());

        return categoryId;
    }

    @Override
    @CacheEvict(value = "categoryTree", allEntries = true)
    public void update(CategoryUpdateCommand updateCommand) {
        Category category = loadCategoryPort.getById(updateCommand.categoryId());
        category.update(updateCommand.name(), updateCommand.description());
        saveCategoryPort.save(category);
    }

    @Override
    @CacheEvict(value = "categoryTree", allEntries = true)
    public void delete(Long categoryId) {
        Category category = loadCategoryPort.getById(categoryId);

        if (category.isDeleted()) {
            throw new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Already deleted category: " + categoryId
            );
        }

        if (loadCategoryPort.hasActiveChildren(categoryId)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot delete category with active children: " + categoryId
            );
        }

        category.delete();
        saveCategoryPort.save(category);

        // Closure Table 엔트리 삭제
        categoryClosurePort.deleteClosureForCategory(categoryId);
    }
}
