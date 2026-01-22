package com.spartaecommerce.order.adapter.out.persistence.jpa.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "orders",
    indexes = {
        // 1) 유저별 주문 조회 + 기간/정렬 (가장 자주 씀)
        @Index(name = "idx_orders_user_created", columnList = "user_id, created_at"),

        // 2) 상태별 조회/집계 또는 exists 서브쿼리 최적화 보조
        @Index(name = "idx_orders_status_order", columnList = "status, order_id"),

        // 3) 판매 통계 조회 최적화 (상태 + 기간별 필터링)
        @Index(name = "idx_orders_status_created", columnList = "status, created_at")
    }
)
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(precision = 15, scale = 2)
    private BigDecimal usedPointAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal earnedPointAmount;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItemJpaEntity> orderItems = new ArrayList<>();

    public static OrderJpaEntity from(Order order) {
        List<OrderItemJpaEntity> orderItemJpaEntities = order.getOrderItems().stream()
            .map(OrderItemJpaEntity::from)
            .toList();

        return OrderJpaEntity.builder()
            .orderId(order.getOrderId())
            .userId(order.getUserId())
            .status(order.getStatus())
            .shippingAddress(order.getShippingAddress())
            .usedPointAmount(order.getUsedPointAmount() != null ? order.getUsedPointAmount().amount() : BigDecimal.ZERO)
            .earnedPointAmount(order.getEarnedPointAmount() != null ? order.getEarnedPointAmount().amount() : BigDecimal.ZERO)
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .orderItems(orderItemJpaEntities)
            .build();
    }

    public Order toDomain() {
        List<OrderItem> orderItemList = this.orderItems.stream()
            .map(OrderItemJpaEntity::toDomain)
            .toList();

        return Order.builder()
            .orderId(this.orderId)
            .userId(this.userId)
            .status(this.status)
            .orderItems(orderItemList)
            .shippingAddress(this.shippingAddress)
            .usedPointAmount(this.usedPointAmount != null ? Money.from(this.usedPointAmount) : Money.zero())
            .earnedPointAmount(this.earnedPointAmount != null ? Money.from(this.earnedPointAmount) : Money.zero())
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}
