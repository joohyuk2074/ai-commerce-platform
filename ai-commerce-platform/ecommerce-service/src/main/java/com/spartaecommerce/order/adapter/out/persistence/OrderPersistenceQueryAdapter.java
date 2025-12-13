package com.spartaecommerce.order.adapter.out.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.common.domain.DateRange;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.adapter.out.persistence.jpa.entity.OrderHistoryJpaEntity;
import com.spartaecommerce.order.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import com.spartaecommerce.order.adapter.out.persistence.jpa.repository.OrderJpaRepository;
import com.spartaecommerce.order.adapter.out.persistence.jpa.repository.OrderStatusHistoryJpaRepository;
import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderHistory;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.domain.port.out.LoadOrderHistoryPort;
import com.spartaecommerce.order.domain.port.out.LoadOrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.spartaecommerce.order.adapter.out.persistence.jpa.entity.QOrderItemJpaEntity.orderItemJpaEntity;
import static com.spartaecommerce.order.adapter.out.persistence.jpa.entity.QOrderJpaEntity.orderJpaEntity;

/**
 * Outbound Adapter for Order Persistence (Query/Read operations)
 * Implements ports for loading orders and order history using QueryDSL
 */
@Component
@RequiredArgsConstructor
public class OrderPersistenceQueryAdapter implements LoadOrderPort, LoadOrderHistoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Order getById(Long orderId) {
        OrderJpaEntity orderJpaEntity = orderJpaRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "orderId: " + orderId));

        return orderJpaEntity.toDomain();
    }

    @Override
    public Page<Order> search(OrderSearchQuery searchQuery) {
        BooleanBuilder conditions = buildSearchConditions(searchQuery);

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(
            searchQuery.pageable().sortBy(),
            searchQuery.pageable().direction()
        );

        Pageable pageable = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        List<Order> orders = fetchOrders(conditions, orderSpecifier, pageable);
        long total = countOrders(conditions);

        return new PageImpl<>(orders, pageable, total);
    }

    @Override
    public boolean existsByProductInOrdersWithStatus(Long productId, OrderStatus orderStatus) {
        if (productId == null || orderStatus == null) {
            return false;
        }

        Integer result = queryFactory
            .selectOne()
            .from(orderItemJpaEntity)
            .where(
                orderItemJpaEntity.productId.eq(productId),
                JPAExpressions
                    .selectOne()
                    .from(orderJpaEntity)
                    .where(
                        orderJpaEntity.orderId.eq(orderItemJpaEntity.orderId),
                        orderJpaEntity.status.eq(orderStatus)
                    )
                    .exists()
            )
            .fetchFirst();

        return result != null;
    }

    @Override
    public List<OrderHistory> findByOrderId(Long orderId) {
        List<OrderHistoryJpaEntity> jpaEntities =
            orderStatusHistoryJpaRepository.findByOrderIdOrderByChangedAtDesc(orderId);

        return jpaEntities.stream()
            .map(OrderHistoryJpaEntity::toDomain)
            .toList();
    }

    private BooleanBuilder buildSearchConditions(OrderSearchQuery searchQuery) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userIdEquals(searchQuery.userId()));
        builder.and(statusEquals(searchQuery.orderStatus()));
        builder.and(createdAtAfter(searchQuery.dateRange()));
        builder.and(createdAtBefore(searchQuery.dateRange()));
        return builder;
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortBy, String direction) {
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        return switch (sortBy.toLowerCase()) {
            case "id", "orderId", "order_id" -> isAsc ? orderJpaEntity.orderId.asc() : orderJpaEntity.orderId.desc();
            case "createdAt", "created_at" -> isAsc ? orderJpaEntity.createdAt.asc() : orderJpaEntity.createdAt.desc();
            default -> orderJpaEntity.createdAt.desc();
        };
    }

    private List<Order> fetchOrders(
        BooleanBuilder conditions,
        OrderSpecifier<?> orderSpecifier,
        Pageable pageable
    ) {
        List<OrderJpaEntity> orderJpaEntities = queryFactory
            .selectFrom(orderJpaEntity)
            .leftJoin(orderJpaEntity.orderItems, orderItemJpaEntity).fetchJoin()
            .where(conditions)
            .orderBy(orderSpecifier)
            .distinct()
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return orderJpaEntities.stream()
            .map(OrderJpaEntity::toDomain)
            .toList();
    }

    private long countOrders(BooleanBuilder conditions) {
        Long count = queryFactory
            .select(orderJpaEntity.count())
            .from(orderJpaEntity)
            .where(conditions)
            .fetchOne();

        return count != null ? count : 0L;
    }

    private BooleanExpression userIdEquals(Long userId) {
        return userId != null ? orderJpaEntity.userId.eq(userId) : null;
    }

    private BooleanExpression statusEquals(OrderStatus status) {
        return status != null ? orderJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression createdAtAfter(DateRange dateRange) {
        if (dateRange == null || !dateRange.hasStartDate()) {
            return null;
        }
        return orderJpaEntity.createdAt.goe(dateRange.startDate());
    }

    private BooleanExpression createdAtBefore(DateRange dateRange) {
        if (dateRange == null || !dateRange.hasEndDate()) {
            return null;
        }
        return orderJpaEntity.createdAt.loe(dateRange.endDate());
    }
}
