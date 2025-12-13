package com.spartaecommerce.order.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.order.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
}
