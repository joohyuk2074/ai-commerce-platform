package com.spartaecommerce.coupon.domain.entity;

/**
 * 쿠폰 적용 범위
 */
public enum ScopeType {
    /**
     * 전체 상품에 적용 가능
     */
    ALL,

    /**
     * 특정 카테고리의 상품에만 적용 가능
     */
    CATEGORY,

    /**
     * 특정 상품에만 적용 가능
     */
    PRODUCT
}
