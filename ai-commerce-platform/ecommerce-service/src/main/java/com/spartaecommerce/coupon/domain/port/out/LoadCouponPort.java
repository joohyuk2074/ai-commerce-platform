package com.spartaecommerce.coupon.domain.port.out;

import com.spartaecommerce.coupon.application.dto.query.CouponSearchQuery;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 쿠폰 조회 포트 (Outbound Port)
 */
public interface LoadCouponPort {

    Optional<Coupon> findById(Long couponId);

    Coupon getById(Long couponId);

    Optional<Coupon> findByName(String couponName);

    boolean existsByName(String couponName);

    /**
     * 특정 상품에 적용 가능한 활성 쿠폰 목록을 조회합니다
     * (전체 상품 쿠폰 + 해당 카테고리 쿠폰 + 해당 상품 쿠폰)
     *
     * @param productId  상품 ID
     * @param categoryId 상품의 카테고리 ID
     * @param now        현재 시각
     * @return 적용 가능한 활성 쿠폰 목록
     */
    List<Coupon> findApplicableCouponsForProduct(Long productId, Long categoryId, LocalDateTime now);

    Page<Coupon> search(CouponSearchQuery query);
}
