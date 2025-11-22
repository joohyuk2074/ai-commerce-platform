package com.spartaecommerce.order.infrastructure.persistence.jpa.repository;

import com.spartaecommerce.order.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    List<OrderJpaEntity> findAllByUserId(Long userId);
}
