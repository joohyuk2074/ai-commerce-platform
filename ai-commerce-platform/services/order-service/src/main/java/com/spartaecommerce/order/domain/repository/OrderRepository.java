package com.spartaecommerce.order.domain.repository;

import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import org.springframework.data.domain.Page;

public interface OrderRepository {

    Long save(Order order);

    boolean existsByProductInOrdersWithStatus(Long productId, OrderStatus orderStatus);

    Page<Order> search(OrderSearchQuery searchQuery);

    Order getById(Long orderId);
}
