package com.spartaecommerce.order.application;

import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.application.dto.result.OrderResult;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.port.in.OrderQueryUseCase;
import com.spartaecommerce.order.domain.port.out.LoadOrderPort;
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
public class OrderQueryService implements OrderQueryUseCase {

    private final LoadOrderPort loadOrderPort;

    /**
     * 주문 검색 (페이징)
     */
    @Override
    public Page<OrderResult> search(OrderSearchQuery searchQuery) {
        Page<Order> orders = loadOrderPort.search(searchQuery);
        return orders.map(OrderResult::from);
    }

    /**
     * 주문 상세 조회
     */
    @Override
    public OrderResult getById(Long orderId) {
        Order order = loadOrderPort.getById(orderId);
        return OrderResult.from(order);
    }
}