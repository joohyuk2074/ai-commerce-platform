package com.spartaecommerce.order.domain.port.out;

import com.spartaecommerce.order.domain.entity.OrderHistory;

import java.util.List;

/**
 * Outbound Port (Secondary/Driven Port) for Order History Retrieval
 * Defines contract for loading order state transition history
 */
public interface LoadOrderHistoryPort {

    /**
     * Retrieves all history records for an order
     *
     * @param orderId order ID
     * @return list of history records in chronological order
     */
    List<OrderHistory> findByOrderId(Long orderId);
}
