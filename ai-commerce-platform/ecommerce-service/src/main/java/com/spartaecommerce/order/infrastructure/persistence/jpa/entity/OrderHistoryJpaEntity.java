package com.spartaecommerce.order.infrastructure.persistence.jpa.entity;

import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.domain.entity.OrderHistory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderHistoryId;

    @Column(nullable = false)
    private Long orderId;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderStatus fromStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus toStatus;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime changedAt;

    public static OrderHistoryJpaEntity from(OrderHistory history) {
        return OrderHistoryJpaEntity.builder()
            .orderHistoryId(history.getOrderHistoryId())
            .orderId(history.getOrderId())
            .fromStatus(history.getFromStatus())
            .toStatus(history.getToStatus())
            .reason(history.getReason())
            .changedAt(history.getChangedAt())
            .build();
    }

    public OrderHistory toDomain() {
        return OrderHistory.builder()
            .orderHistoryId(this.orderHistoryId)
            .orderId(this.orderId)
            .fromStatus(this.fromStatus)
            .toStatus(this.toStatus)
            .reason(this.reason)
            .changedAt(this.changedAt)
            .build();
    }
}
