package com.spartaecommerce.coupon.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.coupon.application.dto.query.CouponSearchQuery;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.entity.ScopeType;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponPort;
import com.spartaecommerce.coupon.domain.port.out.SaveCouponPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeCouponRepository implements LoadCouponPort, SaveCouponPort {

    private final Map<Long, Coupon> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Long save(Coupon coupon) {
        if (coupon.getCouponId() == null) {
            Long newId = idGenerator.getAndIncrement();
            Coupon newCoupon = Coupon.builder()
                .couponId(newId)
                .couponName(coupon.getCouponName())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .scope(coupon.getScope())
                .validityPeriod(coupon.getValidityPeriod())
                .usageLimit(coupon.getUsageLimit())
                .issuedCount(coupon.getIssuedCount())
                .usedCount(coupon.getUsedCount())
                .deleted(coupon.isDeleted())
                .deletedAt(coupon.getDeletedAt())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            store.put(newId, newCoupon);
            return newId;
        } else {
            Coupon updated = Coupon.builder()
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .scope(coupon.getScope())
                .validityPeriod(coupon.getValidityPeriod())
                .usageLimit(coupon.getUsageLimit())
                .issuedCount(coupon.getIssuedCount())
                .usedCount(coupon.getUsedCount())
                .deleted(coupon.isDeleted())
                .deletedAt(coupon.getDeletedAt())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            store.put(coupon.getCouponId(), updated);
            return coupon.getCouponId();
        }
    }

    @Override
    public Optional<Coupon> findById(Long couponId) {
        Coupon coupon = store.get(couponId);
        if (coupon == null || coupon.isDeleted()) {
            return Optional.empty();
        }
        return Optional.of(coupon);
    }

    @Override
    public Coupon getById(Long couponId) {
        return findById(couponId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Coupon not found. couponId: " + couponId
            ));
    }

    @Override
    public Page<Coupon> search(CouponSearchQuery searchQuery) {
        List<Coupon> filtered = store.values().stream()
            .filter(coupon -> !coupon.isDeleted())
            .filter(coupon -> {
                if (searchQuery.isActive() == null) {
                    return true;
                } else if (searchQuery.isActive()) {
                    return coupon.isActive(searchQuery.now());
                } else {
                    return !coupon.isActive(searchQuery.now());
                }
            })
            .sorted(Comparator.comparing(Coupon::getCreatedAt).reversed())
            .collect(Collectors.toList());

        int page = searchQuery.pageable().page();
        int size = searchQuery.pageable().size();
        int start = page * size;
        int end = Math.min(start + size, filtered.size());

        List<Coupon> pageContent = start < filtered.size() ?
            filtered.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(pageContent, PageRequest.of(page, size), filtered.size());
    }

    public List<Coupon> findAllActiveCoupons(LocalDateTime now) {
        return store.values().stream()
            .filter(coupon -> !coupon.isDeleted())
            .filter(coupon -> coupon.isActive(now))
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String couponName) {
        return store.values().stream()
            .filter(coupon -> !coupon.isDeleted())
            .anyMatch(coupon -> coupon.getCouponName().equals(couponName));
    }

    @Override
    public Optional<Coupon> findByName(String couponName) {
        return store.values().stream()
            .filter(coupon -> !coupon.isDeleted())
            .filter(coupon -> coupon.getCouponName().equals(couponName))
            .findFirst();
    }

    @Override
    public List<Coupon> findApplicableCouponsForProduct(Long productId, Long categoryId, LocalDateTime now) {
        return store.values().stream()
            .filter(coupon -> !coupon.isDeleted())
            .filter(coupon -> coupon.isActive(now))
            .filter(coupon -> {
                ScopeType scopeType = coupon.getScopeType();
                Long scopeId = coupon.getScopeId();

                return switch (scopeType) {
                    case ALL -> true;
                    case CATEGORY -> categoryId != null && categoryId.equals(scopeId);
                    case PRODUCT -> productId != null && productId.equals(scopeId);
                };
            })
            .collect(Collectors.toList());
    }
}
