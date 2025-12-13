package com.spartaecommerce.category.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.category.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    boolean existsById(Long categoryId);

    boolean existsByName(String name);
}
