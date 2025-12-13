package com.spartaecommerce.coupon.adapter.out.persistence.jpa.entity;

import com.spartaecommerce.coupon.domain.entity.CouponStatus;
import com.spartaecommerce.coupon.domain.entity.CouponUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 쿠폰 JPA 엔티티
 */
@Entity
@Table(
        name = "coupon_user",
        indexes = {
                @Index(name = "idx_coupon_user_code", columnList = "code", unique = true),
                @Index(name = "idx_coupon_user_user_id", columnList = "user_id"),
                @Index(name = "idx_coupon_user_coupon_id", columnList = "coupon_id"),
                @Index(name = "idx_coupon_user_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponUserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_user_id")
    private Long couponUserId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CouponStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 도메인 엔티티로부터 JPA 엔티티를 생성합니다
     */
    public static CouponUserJpaEntity from(CouponUser couponUser) {
        return new CouponUserJpaEntity(
                couponUser.getCouponUserId(),
                couponUser.getUserId(),
                couponUser.getCouponId(),
                couponUser.getCode(),
                couponUser.getStatus(),
                couponUser.getCreatedAt(),
                couponUser.getUpdatedAt(),
                couponUser.getVersion()
        );
    }

    /**
     * JPA 엔티티를 도메인 엔티티로 변환합니다
     */
    public CouponUser toDomain() {
        return CouponUser.reconstitute(
                this.couponUserId,
                this.userId,
                this.couponId,
                this.code,
                this.status,
                this.createdAt,
                this.updatedAt,
                this.version
        );
    }
}
