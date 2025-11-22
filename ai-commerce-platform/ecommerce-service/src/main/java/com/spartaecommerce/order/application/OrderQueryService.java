package com.spartaecommerce.order.application;

import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.application.dto.result.OrderResult;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 Query 서비스 (CQRS - Read)
 * - 주문 조회
 * - 주문 검색
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;

    /**
     * 주문 검색 (페이징)
     */
    public Page<OrderResult> search(OrderSearchQuery searchQuery) {
        Page<Order> orders = orderRepository.search(searchQuery);
        return orders.map(OrderResult::from);
    }

    /**
     * 주문 상세 조회
     */
    public OrderResult getById(Long orderId) {
        Order order = orderRepository.getById(orderId);
        return OrderResult.from(order);
    }
}