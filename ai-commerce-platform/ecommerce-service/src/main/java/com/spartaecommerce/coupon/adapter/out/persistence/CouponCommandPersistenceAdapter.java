package com.spartaecommerce.coupon.adapter.out.persistence;

import com.spartaecommerce.coupon.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import com.spartaecommerce.coupon.adapter.out.persistence.jpa.repository.CouponJpaRepository;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.port.out.SaveCouponPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponCommandPersistenceAdapter implements SaveCouponPort {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Long save(Coupon coupon) {
        CouponJpaEntity entity = CouponJpaEntity.from(coupon);
        return couponJpaRepository.save(entity).getCouponId();
    }
}
