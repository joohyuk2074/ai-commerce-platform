package com.spartaecommerce.product.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.product.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    boolean existsByName(String name);

    Optional<ProductJpaEntity> findByName(String name);

    Optional<ProductJpaEntity> findByProductIdAndDeleted(Long productId, boolean deleted);

    List<ProductJpaEntity> findAllByProductIdIn(Collection<Long> productIds);

}
