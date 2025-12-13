package com.spartaecommerce.coupon.adapter.out.persistence.jpa.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.coupon.adapter.out.persistence.jpa.converter.DiscountValueConverter;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.entity.ScopeType;
import com.spartaecommerce.coupon.domain.value.CouponScope;
import com.spartaecommerce.coupon.domain.value.DiscountValue;
import com.spartaecommerce.coupon.domain.value.ValidityPeriod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "coupon",
    indexes = {
        @Index(name = "idx_coupon_active_search", columnList = "deleted, startDate, endDate"),
        @Index(name = "idx_coupon_name", columnList = "couponName"),
        @Index(name = "idx_coupon_dates", columnList = "startDate, endDate"),
        @Index(name = "idx_coupon_scope", columnList = "scopeType, scopeId")
    }
)
public class CouponJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(nullable = false, length = 100)
    private String couponName;

    @Convert(converter = DiscountValueConverter.class)
    @Column(nullable = false, length = 50)
    private DiscountValue discountValue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScopeType scopeType;

    @Column
    private Long scopeId;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Integer usageLimit;

    @Column(nullable = false)
    private Integer issuedCount;

    @Column(nullable = false)
    private Integer usedCount;

    @Column(nullable = false)
    private boolean deleted;

    @Column
    private LocalDateTime deletedAt;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public static CouponJpaEntity from(Coupon coupon) {
        CouponScope scope = coupon.getScope();
        ValidityPeriod validityPeriod = coupon.getValidityPeriod();

        return new CouponJpaEntity(
            coupon.getCouponId(),
            coupon.getCouponName(),
            coupon.getDiscountValue(),
            coupon.getMinOrderAmount().amount(),
            coupon.getMaxDiscountAmount() != null ? coupon.getMaxDiscountAmount().amount() : null,
            scope.scopeType(),
            scope.scopeId(),
            validityPeriod.startDate(),
            validityPeriod.endDate(),
            coupon.getUsageLimit(),
            coupon.getIssuedCount(),
            coupon.getUsedCount(),
            coupon.isDeleted(),
            coupon.getDeletedAt(),
            coupon.getCreatedAt(),
            coupon.getUpdatedAt(),
            null
        );
    }

    public Coupon toDomain() {
        CouponScope scope = createCouponScope();
        ValidityPeriod validityPeriod = ValidityPeriod.of(this.startDate, this.endDate);

        return Coupon.builder()
            .couponId(this.couponId)
            .couponName(this.couponName)
            .discountValue(this.discountValue)
            .minOrderAmount(Money.from(this.minOrderAmount))
            .maxDiscountAmount(this.maxDiscountAmount != null ? Money.from(this.maxDiscountAmount) : null)
            .scope(scope)
            .validityPeriod(validityPeriod)
            .usageLimit(this.usageLimit)
            .issuedCount(this.issuedCount)
            .usedCount(this.usedCount)
            .deleted(this.deleted)
            .deletedAt(this.deletedAt)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }

    private CouponScope createCouponScope() {
        return switch (this.scopeType) {
            case ALL -> CouponScope.forAllProducts();
            case CATEGORY -> CouponScope.forCategory(this.scopeId);
            case PRODUCT -> CouponScope.forProduct(this.scopeId);
        };
    }
}
