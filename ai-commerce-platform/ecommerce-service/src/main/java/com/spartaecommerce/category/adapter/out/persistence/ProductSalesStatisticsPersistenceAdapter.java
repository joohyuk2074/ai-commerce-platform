package com.spartaecommerce.category.adapter.out.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.category.application.dto.result.ProductSalesRankingResult;
import com.spartaecommerce.category.domain.port.out.LoadProductSalesStatisticsPort;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.spartaecommerce.category.adapter.out.persistence.jpa.entity.QCategoryJpaEntity.categoryJpaEntity;
import static com.spartaecommerce.order.adapter.out.persistence.jpa.entity.QOrderItemJpaEntity.orderItemJpaEntity;
import static com.spartaecommerce.order.adapter.out.persistence.jpa.entity.QOrderJpaEntity.orderJpaEntity;
import static com.spartaecommerce.product.adapter.out.persistence.jpa.entity.QProductJpaEntity.productJpaEntity;

@Repository
@RequiredArgsConstructor
public class ProductSalesStatisticsPersistenceAdapter implements LoadProductSalesStatisticsPort {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductSalesRankingResult> getProductSalesStatistics(LocalDate startDate, LocalDate endDate) {
        // LocalDate를 LocalDateTime으로 변환 (시작: 00:00:00, 종료: 23:59:59)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 기간별 상품 판매량 집계 (완료된 주문만)
        return queryFactory
            .select(Projections.constructor(
                ProductSalesRankingResult.class,
                productJpaEntity.productId,
                productJpaEntity.name,
                categoryJpaEntity.categoryId,
                categoryJpaEntity.name,
                orderItemJpaEntity.quantity.sum().longValue()
            ))
            .from(orderItemJpaEntity)
            .innerJoin(orderJpaEntity).on(orderItemJpaEntity.orderId.eq(orderJpaEntity.orderId))
            .innerJoin(productJpaEntity).on(orderItemJpaEntity.productId.eq(productJpaEntity.productId))
            .innerJoin(categoryJpaEntity).on(productJpaEntity.categoryId.eq(categoryJpaEntity.categoryId))
            .where(
                orderStatusCompleted(),
                orderCreatedBetween(startDateTime, endDateTime),
                productNotDeleted(),
                categoryNotDeleted()
            )
            .groupBy(
                productJpaEntity.productId,
                productJpaEntity.name,
                categoryJpaEntity.categoryId,
                categoryJpaEntity.name
            )
            .orderBy(
                categoryJpaEntity.categoryId.asc(),
                orderItemJpaEntity.quantity.sum().desc()
            )
            .fetch();
    }

    private BooleanExpression orderStatusCompleted() {
        return orderJpaEntity.status.eq(OrderStatus.COMPLETED);
    }

    private BooleanExpression orderCreatedBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return orderJpaEntity.createdAt.between(startDateTime, endDateTime);
    }

    private BooleanExpression productNotDeleted() {
        return productJpaEntity.deleted.isFalse();
    }

    private BooleanExpression categoryNotDeleted() {
        return categoryJpaEntity.deleted.isFalse();
    }
}
