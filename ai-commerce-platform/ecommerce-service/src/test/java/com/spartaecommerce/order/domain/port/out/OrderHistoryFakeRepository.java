package com.spartaecommerce.order.domain.port.out;

import com.spartaecommerce.order.domain.entity.OrderHistory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderHistoryFakeRepository implements LoadOrderHistoryPort, SaveOrderHistoryPort {

    private final Map<Long, OrderHistory> repository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(OrderHistory orderHistory) {
        if (orderHistory.getOrderHistoryId() == null) {
            long historyId = idGenerator.getAndIncrement();
            OrderHistory newHistory = OrderHistory.builder()
                .orderHistoryId(historyId)
                .orderId(orderHistory.getOrderId())
                .fromStatus(orderHistory.getFromStatus())
                .toStatus(orderHistory.getToStatus())
                .reason(orderHistory.getReason())
                .changedAt(orderHistory.getChangedAt() != null ? orderHistory.getChangedAt() : LocalDateTime.now())
                .build();
            repository.put(historyId, newHistory);
            return historyId;
        } else {
            repository.put(orderHistory.getOrderHistoryId(), orderHistory);
            return orderHistory.getOrderHistoryId();
        }
    }

    @Override
    public List<OrderHistory> findByOrderId(Long orderId) {
        return repository.values().stream()
            .filter(history -> history.getOrderId().equals(orderId))
            .sorted(Comparator.comparing(OrderHistory::getChangedAt).reversed())
            .toList();
    }

    public void clear() {
        repository.clear();
        idGenerator.set(1L);
    }
}
