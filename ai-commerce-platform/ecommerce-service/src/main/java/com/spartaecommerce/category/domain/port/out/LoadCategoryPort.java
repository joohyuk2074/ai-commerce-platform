package com.spartaecommerce.category.domain.port.out;

import com.spartaecommerce.category.domain.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 카테고리 조회 포트 (Outbound Port)
 */
public interface LoadCategoryPort {

    Optional<Category> findById(Long categoryId);

    Category getById(Long categoryId);

    boolean existsById(Long categoryId);

    boolean existsByName(String name);

    boolean hasActiveChildren(Long categoryId);

    List<Category> findAllByParentId(Long parentCategoryId);

    List<Category> findAllByCategoryIdIn(Set<Long> categoryIds);

    Category getExternalCategory();
}