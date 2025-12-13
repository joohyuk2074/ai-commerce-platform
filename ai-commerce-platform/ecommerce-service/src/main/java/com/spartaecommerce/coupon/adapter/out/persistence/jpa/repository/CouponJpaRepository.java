package com.spartaecommerce.coupon.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.coupon.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, Long> {

    Optional<CouponJpaEntity> findByCouponName(String couponName);

    boolean existsByCouponNameAndDeletedFalse(String couponName);
}
