package com.spartaecommerce.order.infrastructure.persistence.jpa.repository;

import com.spartaecommerce.order.domain.entity.OrderHistory;
import com.spartaecommerce.order.domain.repository.OrderHistoryRepository;
import com.spartaecommerce.order.infrastructure.persistence.jpa.entity.OrderHistoryJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderHistoryRepositoryImpl implements OrderHistoryRepository {

    private final OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;

    @Override
    public Long save(OrderHistory history) {
        OrderHistoryJpaEntity jpaEntity = OrderHistoryJpaEntity.from(history);
        return orderStatusHistoryJpaRepository.save(jpaEntity).getOrderHistoryId();
    }

    @Override
    public List<OrderHistory> findByOrderId(Long orderId) {
        List<OrderHistoryJpaEntity> jpaEntities =
            orderStatusHistoryJpaRepository.findByOrderIdOrderByChangedAtDesc(orderId);

        return jpaEntities.stream()
            .map(OrderHistoryJpaEntity::toDomain)
            .toList();
    }
}