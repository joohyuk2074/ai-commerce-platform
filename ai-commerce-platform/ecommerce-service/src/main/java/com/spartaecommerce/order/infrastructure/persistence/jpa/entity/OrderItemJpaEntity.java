package com.spartaecommerce.order.infrastructure.persistence.jpa.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.order.domain.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
/* TODO: -- 인덱스 추가 고려
CREATE INDEX idx_orders_status_id ON orders (status, id);
 */
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private BigDecimal productPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static OrderItemJpaEntity from(OrderItem orderItem) {
        return OrderItemJpaEntity.builder()
            .orderItemId(orderItem.getOrderItemId())
            .productId(orderItem.getProductId())
            .productName(orderItem.getProductName())
            .productPrice(orderItem.getProductPrice().amount())
            .quantity(orderItem.getQuantity())
            .createdAt(orderItem.getCreatedAt())
            .updatedAt(orderItem.getUpdatedAt())
            .build();
    }

    public OrderItem toDomain() {
        return OrderItem.builder()
            .orderItemId(this.orderId)
            .productId(this.productId)
            .productName(this.productName)
            .productPrice(Money.from(this.productPrice))
            .quantity(this.quantity)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}
