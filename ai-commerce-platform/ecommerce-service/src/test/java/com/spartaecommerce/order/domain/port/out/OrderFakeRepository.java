package com.spartaecommerce.order.domain.port.out;

import com.spartaecommerce.common.domain.DateRange;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class OrderFakeRepository implements LoadOrderPort, SaveOrderPort {

    private final Map<Long, Order> repository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(Order order) {
        if (order.getOrderId() == null) {
            long orderId = idGenerator.getAndIncrement();
            Order newOrder = Order.builder()
                .orderId(orderId)
                .userId(order.getUserId())
                .status(order.getStatus())
                .orderItems(order.getOrderItems())
                .shippingAddress(order.getShippingAddress())
                .usedPointAmount(order.getUsedPointAmount())
                .earnedPointAmount(order.getEarnedPointAmount())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(orderId, newOrder);
            return orderId;
        } else {
            Order updatedOrder = Order.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .orderItems(order.getOrderItems())
                .shippingAddress(order.getShippingAddress())
                .usedPointAmount(order.getUsedPointAmount())
                .earnedPointAmount(order.getEarnedPointAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(order.getOrderId(), updatedOrder);
            return order.getOrderId();
        }
    }

    @Override
    public Order getById(Long orderId) {
        return repository.values().stream()
            .filter(order -> order.getOrderId().equals(orderId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "orderId: " + orderId));
    }

    @Override
    public Page<Order> search(OrderSearchQuery searchQuery) {
        Stream<Order> stream = repository.values().stream();

        // 필수: userId 필터
        if (searchQuery.userId() != null) {
            stream = stream.filter(order -> order.getUserId().equals(searchQuery.userId()));
        }

        // 선택: 상태 필터
        if (searchQuery.orderStatus() != null) {
            stream = stream.filter(order -> order.getStatus() == searchQuery.orderStatus());
        }

        // 선택: 날짜 범위 필터
        if (searchQuery.dateRange() != null) {
            DateRange dateRange = searchQuery.dateRange();
            if (dateRange.hasStartDate()) {
                stream = stream.filter(order ->
                    !order.getCreatedAt().isBefore(dateRange.startDate())
                );
            }
            if (dateRange.hasEndDate()) {
                stream = stream.filter(order ->
                    !order.getCreatedAt().isAfter(dateRange.endDate())
                );
            }
        }

        List<Order> filteredOrders = stream.toList();

        // 정렬
        Comparator<Order> comparator = getComparator(
            searchQuery.pageable().sortBy(),
            searchQuery.pageable().direction()
        );
        List<Order> sortedOrders = filteredOrders.stream()
            .sorted(comparator)
            .toList();

        PageRequest pageRequest = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), sortedOrders.size());

        List<Order> pageContent = sortedOrders.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, sortedOrders.size());
    }

    @Override
    public boolean existsByProductInOrdersWithStatus(Long productId, OrderStatus orderStatus) {
        return repository.values().stream()
            .filter(order -> order.getStatus() == orderStatus)
            .flatMap(order -> order.getOrderItems().stream())
            .anyMatch(item -> item.getProductId().equals(productId));
    }

    private Comparator<Order> getComparator(String sortBy, String direction) {
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        Comparator<Order> comparator = switch (sortBy.toLowerCase()) {
            case "id", "orderid", "order_id" -> Comparator.comparing(Order::getOrderId);
            case "createdat", "created_at" -> Comparator.comparing(Order::getCreatedAt);
            default -> Comparator.comparing(Order::getCreatedAt);
        };

        return isAsc ? comparator : comparator.reversed();
    }

    public void clear() {
        repository.clear();
        idGenerator.set(1L);
    }
}
