package com.spartaecommerce.category.application.service;

import com.spartaecommerce.category.application.dto.commnad.CategoryRegisterCommand;
import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.in.CategoryCommandUseCase;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.category.domain.port.out.SaveCategoryPort;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryCommandService implements CategoryCommandUseCase {

    private final LoadCategoryPort loadCategoryPort;
    private final SaveCategoryPort saveCategoryPort;
    private final LoadProductPort loadProductPort;

    @Override
    public Long register(CategoryRegisterCommand registerCommand) {
        if (loadCategoryPort.existsByName(registerCommand.name())) {
            throw new BusinessException(ErrorCode.ENTITY_ALREADY_EXISTS, "Category name: " + registerCommand.name());
        }

        Category category = Category.createNew(
            registerCommand.name(),
            registerCommand.description(),
            registerCommand.parentCategoryId()
        );

        return saveCategoryPort.save(category);
    }

    @Override
    public void update(Long categoryId, CategoryUpdateCommand updateCommand) {
        Category category = loadCategoryPort.getById(categoryId);
        category.update(updateCommand.name(), updateCommand.description());
        saveCategoryPort.save(category);
    }

    @Override
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

        if (loadProductPort.existsByCategoryId(categoryId)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot delete category with active products: " + categoryId
            );
        }

        category.delete();
        saveCategoryPort.save(category);
    }
}
