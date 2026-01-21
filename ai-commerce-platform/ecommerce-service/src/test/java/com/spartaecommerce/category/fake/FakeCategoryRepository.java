package com.spartaecommerce.category.fake;

import com.spartaecommerce.common.domain.category.Category;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.category.domain.port.out.SaveCategoryPort;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 테스트용 In-Memory Category Repository
 * LoadCategoryPort와 SaveCategoryPort를 모두 구현
 */
public class FakeCategoryRepository implements LoadCategoryPort, SaveCategoryPort {

    private final Map<Long, Category> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Long save(Category category) {
        Long categoryId = category.getCategoryId();

        if (categoryId == null) {
            // 새로운 카테고리 생성
            categoryId = idGenerator.getAndIncrement();
            Category savedCategory = Category.builder()
                .categoryId(categoryId)
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategoryId())
                .childrenCategoryIds(category.getChildrenCategoryIds() != null
                    ? new ArrayList<>(category.getChildrenCategoryIds())
                    : new ArrayList<>())
                .deleted(category.isDeleted())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            store.put(categoryId, savedCategory);
        } else {
            // 기존 카테고리 업데이트
            Category existing = store.get(categoryId);
            if (existing != null) {
                Category updated = Category.builder()
                    .categoryId(categoryId)
                    .name(category.getName())
                    .description(category.getDescription())
                    .parentCategoryId(category.getParentCategoryId())
                    .childrenCategoryIds(category.getChildrenCategoryIds() != null
                        ? new ArrayList<>(category.getChildrenCategoryIds())
                        : new ArrayList<>())
                    .deleted(category.isDeleted())
                    .createdAt(existing.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
                store.put(categoryId, updated);
            }
        }

        return categoryId;
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        return Optional.ofNullable(store.get(categoryId));
    }

    @Override
    public Category getById(Long categoryId) {
        return findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }

    @Override
    public boolean existsById(Long categoryId) {
        return store.containsKey(categoryId);
    }

    @Override
    public boolean existsByName(String name) {
        return store.values().stream()
            .anyMatch(category -> category.getName().equals(name) && !category.isDeleted());
    }

    @Override
    public boolean hasActiveChildren(Long categoryId) {
        return store.values().stream()
            .anyMatch(category ->
                Objects.equals(category.getParentCategoryId(), categoryId) && !category.isDeleted());
    }

    @Override
    public List<Category> findAllByCategoryIdIn(Set<Long> categoryIds) {
        return categoryIds.stream()
            .map(store::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public Category getExternalCategory() {
        // 테스트용으로 "외부" 카테고리를 반환
        return store.values().stream()
            .filter(c -> "외부".equals(c.getName()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }

    @Override
    public List<Category> findAll() {
        return store.values().stream()
            .filter(category -> !category.isDeleted())
            .collect(Collectors.toList());
    }

    // 테스트 헬퍼 메서드
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }

    public int size() {
        return store.size();
    }
}
