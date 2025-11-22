package com.spartaecommerce.order.domain.repository;

import com.spartaecommerce.order.domain.entity.OrderHistory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class OrderHistoryFakeRepository implements OrderHistoryRepository {

    private final Map<Long, OrderHistory> histories = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(OrderHistory history) {
        if (history.getOrderHistoryId() == null) {
            long orderHistoryId = idGenerator.getAndIncrement();
            OrderHistory newOrderHistory = OrderHistory.builder()
                .orderHistoryId(orderHistoryId)
                .orderId(history.getOrderId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .build();
            histories.put(orderHistoryId, newOrderHistory);
            return orderHistoryId;
        } else {
            histories.put(history.getOrderHistoryId(), history);
            return history.getOrderHistoryId();
        }
    }

    // 테스트 헬퍼 메서드
    public List<OrderHistory> findByOrderId(Long orderId) {
        return histories.values().stream()
            .filter(h -> orderId.equals(h.getOrderId()))
            .sorted(Comparator.comparing(
                OrderHistory::getOrderHistoryId,
                Comparator.reverseOrder()
            ))
            .toList();
    }

    public void clear() {
        histories.clear();
        idGenerator.set(1L);
    }
}
