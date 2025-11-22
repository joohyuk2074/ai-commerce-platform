package com.spartaecommerce.refund.infrastructure.persistence.jpa.entity;

import com.spartaecommerce.refund.domain.entity.Refund;
import com.spartaecommerce.refund.domain.entity.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefundJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static RefundJpaEntity from(Refund refund) {
        return RefundJpaEntity.builder()
            .refundId(refund.getRefundId())
            .userId(refund.getUserId())
            .orderId(refund.getOrderId())
            .reason(refund.getReason())
            .status(refund.getStatus())
            .createdAt(refund.getCreatedAt())
            .updatedAt(refund.getUpdatedAt())
            .build();
    }

    public Refund toDomain() {
        return Refund.builder()
            .refundId(this.refundId)
            .userId(this.userId)
            .orderId(this.orderId)
            .reason(this.reason)
            .status(this.status)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}