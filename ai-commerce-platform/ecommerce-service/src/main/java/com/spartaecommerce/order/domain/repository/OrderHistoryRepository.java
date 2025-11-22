package com.spartaecommerce.order.domain.repository;

import com.spartaecommerce.order.domain.entity.OrderHistory;

import java.util.List;

public interface OrderHistoryRepository {

    Long save(OrderHistory history);

    List<OrderHistory> findByOrderId(Long orderId);
}
