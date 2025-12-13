package com.spartaecommerce.category.domain.port.out;

import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CategoryFakeRepository implements LoadCategoryPort, SaveCategoryPort {

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
                .childrenCategoryIds(category.getChildrenCategoryIds())
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
                .childrenCategoryIds(category.getChildrenCategoryIds())
                .deleted(category.isDeleted())
                .createdAt(category.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(category.getCategoryId(), updatedCategory);
            return category.getCategoryId();
        }
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        return Optional.ofNullable(repository.get(categoryId))
            .filter(category -> !category.isDeleted());
    }

    @Override
    public Category getById(Long categoryId) {
        return findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "categoryId: " + categoryId));
    }

    @Override
    public boolean existsById(Long categoryId) {
        return repository.values().stream()
            .filter(category -> !category.isDeleted())
            .anyMatch(category -> category.getCategoryId().equals(categoryId));
    }

    @Override
    public boolean existsByName(String name) {
        return repository.values().stream()
            .filter(category -> !category.isDeleted())
            .anyMatch(category -> category.getName().equals(name));
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
                }
                return parentCategoryId.equals(category.getParentCategoryId());
            })
            .toList();
    }

    @Override
    public List<Category> findAllByCategoryIdIn(Set<Long> categoryIds) {
        return repository.values().stream()
            .filter(category -> !category.isDeleted())
            .filter(category -> categoryIds.contains(category.getCategoryId()))
            .toList();
    }

    public void clear() {
        repository.clear();
        idGenerator.set(1L);
    }
}
