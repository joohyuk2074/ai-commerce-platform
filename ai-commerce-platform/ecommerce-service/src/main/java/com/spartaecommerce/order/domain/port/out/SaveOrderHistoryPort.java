package com.spartaecommerce.order.domain.port.out;

import com.spartaecommerce.order.domain.entity.OrderHistory;

/**
 * Outbound Port (Secondary/Driven Port) for Order History Persistence
 * Defines contract for saving order state transition history
 */
public interface SaveOrderHistoryPort {

    /**
     * Persists an order history record
     *
     * @param orderHistory history domain entity to save
     * @return saved history ID
     */
    Long save(OrderHistory orderHistory);
}
