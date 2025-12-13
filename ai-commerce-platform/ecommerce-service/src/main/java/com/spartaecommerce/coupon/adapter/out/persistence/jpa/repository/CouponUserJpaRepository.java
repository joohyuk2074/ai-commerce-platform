package com.spartaecommerce.coupon.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.coupon.adapter.out.persistence.jpa.entity.CouponUserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 쿠폰 JPA 저장소
 */
public interface CouponUserJpaRepository extends JpaRepository<CouponUserJpaEntity, Long> {

    /**
     * 쿠폰 코드로 조회
     */
    Optional<CouponUserJpaEntity> findByCode(String code);

    /**
     * 쿠폰 코드 존재 여부 확인
     */
    boolean existsByCode(String code);

    /**
     * 주어진 코드 목록 중 존재하는 코드들을 조회 (배치 조회)
     * N+1 문제 해결을 위한 메서드
     */
    @Query("SELECT cu.code FROM CouponUserJpaEntity cu WHERE cu.code IN :codes")
    List<String> findCodesByCodeIn(@Param("codes") Collection<String> codes);

    /**
     * 특정 쿠폰의 발행된 개수 조회
     */
    @Query("SELECT COUNT(cu) FROM CouponUserJpaEntity cu WHERE cu.couponId = :couponId")
    long countByCouponId(@Param("couponId") Long couponId);
}
