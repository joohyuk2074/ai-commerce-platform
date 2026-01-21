package com.spartaecommerce.order.domain.port.in;

import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.application.dto.result.OrderResult;
import org.springframework.data.domain.Page;

/**
 * Inbound Port (Primary/Driving Port) for Order Query Operations
 * Defines use cases for read-only operations on orders
 */
public interface OrderQueryUseCase {

    /**
     * Retrieves a single order by ID
     *
     * @param orderId order ID
     * @return order details with items
     */
    OrderResult getById(Long orderId);

    /**
     * Searches orders with filtering and pagination
     *
     * @param searchQuery search criteria including userId, status, date range
     * @return paginated order results
     */
    Page<OrderResult> search(OrderSearchQuery searchQuery);
}
