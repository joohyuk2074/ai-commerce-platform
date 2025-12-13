package com.spartaecommerce.cart.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.cart.adapter.out.persistence.jpa.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<CartJpaEntity, Long> {

    Optional<CartJpaEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);
}
