package com.spartaecommerce.category.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.category.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    Optional<CategoryJpaEntity> findByName(String name);

    boolean existsById(Long categoryId);

    boolean existsByName(String name);
}
