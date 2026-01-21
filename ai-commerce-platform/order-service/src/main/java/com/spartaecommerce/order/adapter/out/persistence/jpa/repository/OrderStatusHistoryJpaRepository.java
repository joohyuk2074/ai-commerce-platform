package com.spartaecommerce.order.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.order.adapter.out.persistence.jpa.entity.OrderHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryJpaRepository extends JpaRepository<OrderHistoryJpaEntity, Long> {

    List<OrderHistoryJpaEntity> findByOrderIdOrderByChangedAtDesc(Long orderId);
}
