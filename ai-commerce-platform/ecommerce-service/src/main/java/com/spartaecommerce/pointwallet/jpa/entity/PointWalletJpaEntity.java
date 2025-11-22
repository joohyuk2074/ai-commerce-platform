package com.spartaecommerce.pointwallet.jpa.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "point_wallet")
public class PointWalletJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static PointWalletJpaEntity from(PointWallet wallet) {
        return new PointWalletJpaEntity(
            wallet.getWalletId(),
            wallet.getUserId(),
            wallet.getBalance().amount(),
            wallet.isActive(),
            wallet.getCreatedAt(),
            wallet.getUpdatedAt()
        );
    }

    public PointWallet toDomain() {
        return PointWallet.builder()
            .walletId(this.walletId)
            .userId(this.userId)
            .balance(Money.from(this.balance))
            .active(this.active)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}