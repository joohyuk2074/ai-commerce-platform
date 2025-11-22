package com.spartaecommerce.refund.infrastructure.persistence.jpa.repository;

import com.spartaecommerce.refund.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, Long> {

    Optional<RefundJpaEntity> findByOrderId(Long orderId);

    List<RefundJpaEntity> findAllByUserId(Long userId);
}