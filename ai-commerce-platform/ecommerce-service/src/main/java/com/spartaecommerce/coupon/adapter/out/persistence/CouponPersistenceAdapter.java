package com.spartaecommerce.coupon.adapter.out.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.coupon.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import com.spartaecommerce.coupon.adapter.out.persistence.jpa.repository.CouponJpaRepository;
import com.spartaecommerce.coupon.application.dto.query.CouponSearchQuery;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.entity.ScopeType;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.spartaecommerce.coupon.adapter.out.persistence.jpa.entity.QCouponJpaEntity.couponJpaEntity;

@Component
@RequiredArgsConstructor
public class CouponPersistenceAdapter implements LoadCouponPort {

    private final CouponJpaRepository couponJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Coupon getById(Long couponId) {
        CouponJpaEntity couponJpaEntity = couponJpaRepository.findById(couponId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Coupon not found. couponId: " + couponId
            ));

        return couponJpaEntity.toDomain();
    }

    @Override
    public Optional<Coupon> findById(Long couponId) {
        return couponJpaRepository.findById(couponId)
            .map(CouponJpaEntity::toDomain);
    }

    @Override
    public Optional<Coupon> findByName(String couponName) {
        return couponJpaRepository.findByCouponName(couponName)
            .filter(entity -> !entity.isDeleted())
            .map(CouponJpaEntity::toDomain);
    }

    @Override
    public boolean existsByName(String couponName) {
        return couponJpaRepository.existsByCouponNameAndDeletedFalse(couponName);
    }

    @Override
    public List<Coupon> findApplicableCouponsForProduct(Long productId, Long categoryId, LocalDateTime now) {
        BooleanBuilder scopeConditions = new BooleanBuilder();

        // ALL: 전체 상품에 적용 가능
        scopeConditions.or(couponJpaEntity.scopeType.eq(ScopeType.ALL));

        // CATEGORY: 특정 카테고리 상품에 적용 가능
        if (categoryId != null) {
            scopeConditions.or(
                couponJpaEntity.scopeType.eq(ScopeType.CATEGORY)
                    .and(couponJpaEntity.scopeId.eq(categoryId))
            );
        }

        // PRODUCT: 특정 상품에만 적용 가능
        if (productId != null) {
            scopeConditions.or(
                couponJpaEntity.scopeType.eq(ScopeType.PRODUCT)
                    .and(couponJpaEntity.scopeId.eq(productId))
            );
        }

        List<CouponJpaEntity> entities = queryFactory
            .selectFrom(couponJpaEntity)
            .where(
                isNotDeleted(),
                isWithinDateRange(now),
                hasUsageRemaining(),
                scopeConditions
            )
            .fetch();

        return entities.stream()
            .map(CouponJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Page<Coupon> search(CouponSearchQuery searchQuery) {
        BooleanBuilder conditions = buildSearchConditions(searchQuery);

        Pageable pageable = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        List<Coupon> coupons = fetchCoupons(conditions, couponJpaEntity.couponId.desc(), pageable);
        long total = countCoupons(conditions);

        return new PageImpl<>(coupons, pageable, total);
    }

    private BooleanBuilder buildSearchConditions(CouponSearchQuery searchQuery) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(isNotDeleted());

        if (searchQuery.isActive() != null && searchQuery.isActive()) {
            builder.and(isWithinDateRange(searchQuery.now()));
            builder.and(hasUsageRemaining());
        } else {
            BooleanExpression isExpired = couponJpaEntity.endDate.before(searchQuery.now());
            BooleanExpression isNotStarted = couponJpaEntity.startDate.after(searchQuery.now());
            BooleanExpression isExhausted = couponJpaEntity.usedCount.goe(couponJpaEntity.usageLimit);

            builder.and(isExpired.or(isNotStarted).or(isExhausted));
        }

        return builder;
    }

    private List<Coupon> fetchCoupons(
        BooleanBuilder conditions,
        OrderSpecifier<?> orderSpecifier,
        Pageable pageable
    ) {
        List<CouponJpaEntity> entities = queryFactory
            .selectFrom(couponJpaEntity)
            .where(conditions)
            .orderBy(orderSpecifier)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return entities.stream()
            .map(CouponJpaEntity::toDomain)
            .toList();
    }

    private long countCoupons(BooleanBuilder conditions) {
        Long count = queryFactory
            .select(couponJpaEntity.count())
            .from(couponJpaEntity)
            .where(conditions)
            .fetchOne();

        return count != null ? count : 0L;
    }

    private BooleanExpression isNotDeleted() {
        return couponJpaEntity.deleted.eq(false);
    }

    private BooleanExpression isWithinDateRange(LocalDateTime now) {
        return couponJpaEntity.startDate.loe(now)
            .and(couponJpaEntity.endDate.goe(now));
    }

    private BooleanExpression hasUsageRemaining() {
        return couponJpaEntity.usedCount.lt(couponJpaEntity.usageLimit);
    }
}
