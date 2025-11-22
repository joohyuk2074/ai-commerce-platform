package com.spartaecommerce.category.domain.repository;

import com.spartaecommerce.category.domain.entity.Category;

import java.util.List;
import java.util.Set;

public interface CategoryRepository {

    Long save(Category category);

    boolean existsByName(String name);

    Category getById(Long categoryId);

    boolean existsById(Long categoryId);

    boolean hasActiveChildren(Long categoryId);

    List<Category> findAllByParentId(Long categoryId);

    List<Category> findAllByCategoryIdIn(Set<Long> categoryIds);
}
