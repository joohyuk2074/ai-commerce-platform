package com.spartaecommerce.pointwallet.adapter.out.persistence.jpa.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.domain.pointwallet.PointTransaction;
import com.spartaecommerce.common.domain.pointwallet.PointTransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "point_transaction")
public class PointTransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 500)
    private String description;

    @Column
    private LocalDateTime expireAt;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public static PointTransactionJpaEntity from(PointTransaction transaction) {
        return new PointTransactionJpaEntity(
            transaction.getTransactionId(),
            transaction.getWalletId(),
            transaction.getType(),
            transaction.getAmount().amount(),
            transaction.getBalanceAfter().amount(),
            transaction.getDescription(),
            transaction.getExpireAt(),
            transaction.getCreatedAt()
        );
    }

    public PointTransaction toDomain() {
        return PointTransaction.builder()
            .transactionId(this.transactionId)
            .walletId(this.walletId)
            .type(this.type)
            .amount(Money.from(this.amount))
            .balanceAfter(Money.from(this.balanceAfter))
            .description(this.description)
            .expireAt(this.expireAt)
            .createdAt(this.createdAt)
            .build();
    }
}
