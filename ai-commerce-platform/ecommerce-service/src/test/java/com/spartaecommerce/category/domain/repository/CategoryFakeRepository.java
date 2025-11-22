package com.spartaecommerce.category.domain.repository;

import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CategoryFakeRepository implements CategoryRepository {

    private final Map<Long, Category> repository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(Category category) {
        if (category.getCategoryId() == null) {
            long categoryId = idGenerator.getAndIncrement();
            Category newCategory = Category.builder()
                .categoryId(categoryId)
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategoryId())
                .childrenCategoryIds(new ArrayList<>(category.getChildrenCategoryIds()))
                .deleted(category.isDeleted())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(categoryId, newCategory);
            return categoryId;
        } else {
            Category updatedCategory = Category.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategoryId())
                .childrenCategoryIds(new ArrayList<>(category.getChildrenCategoryIds()))
                .deleted(category.isDeleted())
                .createdAt(category.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(category.getCategoryId(), updatedCategory);
            return category.getCategoryId();
        }
    }

    @Override
    public boolean existsByName(String name) {
        return repository.values().stream()
            .filter(category -> !category.isDeleted())
            .anyMatch(category -> category.getName().equals(name));
    }

    @Override
    public Category getById(Long categoryId) {
        Category category = repository.get(categoryId);
        if (category == null || category.isDeleted()) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "categoryId: " + categoryId);
        }
        return category;
    }

    @Override
    public boolean existsById(Long categoryId) {
        return repository.containsKey(categoryId) && !repository.get(categoryId).isDeleted();
    }

    @Override
    public boolean hasActiveChildren(Long categoryId) {
        return repository.values().stream()
            .filter(category -> !category.isDeleted())
            .anyMatch(category -> categoryId.equals(category.getParentCategoryId()));
    }

    @Override
    public List<Category> findAllByParentId(Long parentCategoryId) {
        return repository.values().stream()
            .filter(category -> !category.isDeleted())
            .filter(category -> {
                if (parentCategoryId == null) {
                    return category.getParentCategoryId() == null;
                } else {
                    return parentCategoryId.equals(category.getParentCategoryId());
                }
            })
            .toList();
    }

    @Override
    public List<Category> findAllByCategoryIdIn(java.util.Set<Long> categoryIds) {
        return repository.values().stream()
            .filter(category -> !category.isDeleted())
            .filter(category -> categoryIds.contains(category.getCategoryId()))
            .toList();
    }

    public void clear() {
        repository.clear();
    }
}