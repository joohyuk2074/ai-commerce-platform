package com.spartaecommerce.category.adapter.out.persistence;

import com.spartaecommerce.category.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import com.spartaecommerce.category.adapter.out.persistence.jpa.repository.CategoryJpaRepository;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.out.SaveCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryCommandPersistenceAdapter implements SaveCategoryPort {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Long save(Category category) {
        CategoryJpaEntity categoryJpaEntity = CategoryJpaEntity.from(category);
        return categoryJpaRepository.save(categoryJpaEntity).getCategoryId();
    }
}
