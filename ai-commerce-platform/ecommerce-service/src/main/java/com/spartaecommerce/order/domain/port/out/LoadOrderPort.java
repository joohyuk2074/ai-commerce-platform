package com.spartaecommerce.order.domain.port.out;

import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;

/**
 * Outbound Port (Secondary/Driven Port) for Order Retrieval
 * Defines contract for loading order aggregates
 */
public interface LoadOrderPort {

    /**
     * Retrieves an order by ID, throws exception if not found
     *
     * @param orderId order ID
     * @return order domain entity with items
     * @throws com.spartaecommerce.common.exception.BusinessException if order not found
     */
    Order getById(Long orderId);

    /**
     * Searches orders with filtering and pagination
     *
     * @param searchQuery search criteria
     * @return paginated order results
     */
    Page<Order> search(OrderSearchQuery searchQuery);

    /**
     * Checks if a product exists in orders with specific status
     *
     * @param productId product ID to check
     * @param orderStatus order status filter
     * @return true if product is in any order with given status
     */
    boolean existsByProductInOrdersWithStatus(Long productId, OrderStatus orderStatus);
}
