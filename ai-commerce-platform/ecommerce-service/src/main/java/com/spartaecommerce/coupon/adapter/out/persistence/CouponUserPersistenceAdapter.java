package com.spartaecommerce.coupon.adapter.out.persistence;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.coupon.adapter.out.persistence.jpa.entity.CouponUserJpaEntity;
import com.spartaecommerce.coupon.adapter.out.persistence.jpa.repository.CouponUserJpaRepository;
import com.spartaecommerce.coupon.domain.entity.CouponUser;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponUserPort;
import com.spartaecommerce.coupon.domain.port.out.SaveCouponUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 쿠폰 사용자 영속성 어댑터
 * Outbound ports (LoadCouponUserPort, SaveCouponUserPort)를 구현합니다
 */
@Component
@RequiredArgsConstructor
public class CouponUserPersistenceAdapter implements LoadCouponUserPort, SaveCouponUserPort {

    private final CouponUserJpaRepository couponUserJpaRepository;

    @Override
    public Long save(CouponUser couponUser) {
        CouponUserJpaEntity entity = CouponUserJpaEntity.from(couponUser);
        CouponUserJpaEntity saved = couponUserJpaRepository.save(entity);
        return saved.getCouponUserId();
    }

    @Override
    public List<Long> saveAll(List<CouponUser> couponUsers) {
        List<CouponUserJpaEntity> entities = couponUsers.stream()
            .map(CouponUserJpaEntity::from)
            .toList();

        List<CouponUserJpaEntity> saved = couponUserJpaRepository.saveAll(entities);

        return saved.stream()
            .map(CouponUserJpaEntity::getCouponUserId)
            .toList();
    }

    @Override
    public Optional<CouponUser> findById(Long couponUserId) {
        return couponUserJpaRepository.findById(couponUserId)
            .map(CouponUserJpaEntity::toDomain);
    }

    @Override
    public CouponUser getById(Long couponUserId) {
        return findById(couponUserId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.COUPON_NOT_FOUND,
                "쿠폰을 찾을 수 없습니다. ID: " + couponUserId
            ));
    }

    @Override
    public Optional<CouponUser> findByCode(String code) {
        return couponUserJpaRepository.findByCode(code)
            .map(CouponUserJpaEntity::toDomain);
    }

    @Override
    public CouponUser getByCode(String code) {
        return findByCode(code)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.COUPON_NOT_FOUND,
                "쿠폰을 찾을 수 없습니다. 코드: " + code
            ));
    }

    @Override
    public boolean existsByCode(String code) {
        return couponUserJpaRepository.existsByCode(code);
    }

    @Override
    public Set<String> findExistingCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new HashSet<>();
        }
        List<String> existingCodesList = couponUserJpaRepository.findCodesByCodeIn(codes);
        return new HashSet<>(existingCodesList);
    }

    @Override
    public long countByCouponId(Long couponId) {
        return couponUserJpaRepository.countByCouponId(couponId);
    }
}
