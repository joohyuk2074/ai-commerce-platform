package com.spartaecommerce.order.domain.repository;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.domain.port.out.LoadOrderPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class OrderFakeRepository implements OrderRepository, LoadOrderPort {

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(Order order) {
        if (order.getOrderId() == null) {
            Long orderId = idGenerator.getAndIncrement();
            Order newOrder = Order.builder()
                .orderId(orderId)
                .userId(order.getUserId())
                .status(order.getStatus())
                .orderItems(order.getOrderItems())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
            orders.put(orderId, newOrder);
            return orderId;
        } else {
            orders.put(order.getOrderId(), order);
            return order.getOrderId();
        }
    }

    @Override
    public boolean existsByProductInOrdersWithStatus(Long productId, OrderStatus orderStatus) {
        for (Order order : orders.values()) {
            if (!order.getStatus().equals(orderStatus)) {
                continue;
            }

            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                if (orderItem.getProductId().equals(productId)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Order getById(Long orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Order not found: " + orderId
            );
        }
        return order;
    }

    @Override
    public Page<Order> search(OrderSearchQuery searchQuery) {
        Stream<Order> stream = orders.values().stream();

        // userId 필터
        if (searchQuery.userId() != null) {
            stream = stream.filter(o -> o.getUserId().equals(searchQuery.userId()));
        }

        // status 필터
        if (searchQuery.orderStatus() != null) {
            stream = stream.filter(o -> o.getStatus() == searchQuery.orderStatus());
        }

        // 정렬 (기본: 최신순)
        List<Order> filtered = stream
            .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
            .toList();

        // 페이징
        int page = searchQuery.pageable().page();
        int size = searchQuery.pageable().size();
        int start = page * size;
        int end = Math.min(start + size, filtered.size());

        List<Order> paged = (start < filtered.size())
            ? filtered.subList(start, end)
            : List.of();

        return new PageImpl<>(
            paged,
            PageRequest.of(page, size),
            filtered.size()
        );
    }

    public void clear() {
        orders.clear();
        idGenerator.set(1L);
    }
}