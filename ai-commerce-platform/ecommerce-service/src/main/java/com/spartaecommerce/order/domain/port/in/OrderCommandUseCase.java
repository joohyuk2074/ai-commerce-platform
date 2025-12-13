package com.spartaecommerce.order.domain.port.in;

import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderStatusUpdateCommand;

/**
 * Inbound Port (Primary/Driving Port) for Order Command Operations
 * Defines use cases for state-changing operations on orders
 */
public interface OrderCommandUseCase {

    /**
     * Creates a new order with items, processes stock deduction and points
     *
     * @param createCommand order creation details including items and shipping info
     * @return created order ID
     */
    Long create(OrderCreateCommand createCommand);

    /**
     * Updates the status of an existing order
     * If status is CANCELED, delegates to cancel operation
     *
     * @param updateCommand order ID and new status
     */
    void updateOrderStatus(OrderStatusUpdateCommand updateCommand);

    /**
     * Cancels an order, restoring stock and points
     *
     * @param orderId ID of the order to cancel
     */
    void cancel(Long orderId);
}
