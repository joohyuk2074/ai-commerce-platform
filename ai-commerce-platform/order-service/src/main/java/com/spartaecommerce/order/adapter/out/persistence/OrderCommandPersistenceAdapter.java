package com.spartaecommerce.order.adapter.out.persistence;

import com.spartaecommerce.order.adapter.out.persistence.jpa.entity.OrderHistoryJpaEntity;
import com.spartaecommerce.order.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import com.spartaecommerce.order.adapter.out.persistence.jpa.repository.OrderJpaRepository;
import com.spartaecommerce.order.adapter.out.persistence.jpa.repository.OrderStatusHistoryJpaRepository;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderHistory;
import com.spartaecommerce.order.domain.port.out.SaveOrderHistoryPort;
import com.spartaecommerce.order.domain.port.out.SaveOrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Outbound Adapter for Order Persistence (Command/Write operations)
 * Implements ports for saving orders and order history
 */
@Component
@RequiredArgsConstructor
public class OrderCommandPersistenceAdapter implements SaveOrderPort, SaveOrderHistoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;

    @Override
    public Long save(Order order) {
        OrderJpaEntity orderJpaEntity = OrderJpaEntity.from(order);
        return orderJpaRepository.save(orderJpaEntity).getOrderId();
    }

    @Override
    public Long save(OrderHistory orderHistory) {
        OrderHistoryJpaEntity jpaEntity = OrderHistoryJpaEntity.from(orderHistory);
        return orderStatusHistoryJpaRepository.save(jpaEntity).getOrderHistoryId();
    }
}
