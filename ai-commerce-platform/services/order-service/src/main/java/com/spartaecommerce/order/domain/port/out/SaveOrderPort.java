package com.spartaecommerce.order.domain.port.out;

import com.spartaecommerce.order.domain.entity.Order;

/**
 * Outbound Port (Secondary/Driven Port) for Order Persistence
 * Defines contract for saving order aggregates
 */
public interface SaveOrderPort {

    /**
     * Persists an order aggregate with its items
     *
     * @param order order domain entity to save
     * @return saved order ID
     */
    Long save(Order order);
}
