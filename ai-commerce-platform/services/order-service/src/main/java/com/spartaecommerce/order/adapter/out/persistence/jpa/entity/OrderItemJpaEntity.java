package com.spartaecommerce.order.adapter.out.persistence.jpa.entity;

import com.spartaecommerce.domain.vo.Money;
import com.spartaecommerce.order.domain.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "order_item",
    indexes = {
//        // 1) 주문 -> 아이템 조회(조인/컬렉션 로딩)
//        @Index(name = "idx_order_item_order", columnList = "order_id"),
//
        // 2) 상품이 포함된 주문 존재 체크 (핵심)
        @Index(name = "idx_order_item_product_order", columnList = "product_id, order_id"),
//
//        // 3) (선택) 상품별 아이템 조회가 많으면 created_at까지 포함 고려
//        @Index(name = "idx_order_item_product_created", columnList = "product_id, created_at")
    }
)
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
            .orderItemId(this.orderItemId)
            .productId(this.productId)
            .productName(this.productName)
            .productPrice(Money.from(this.productPrice))
            .quantity(this.quantity)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}
