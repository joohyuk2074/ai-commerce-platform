package com.spartaecommerce.coupon.domain.value;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.coupon.domain.entity.ScopeType;

/**
 * 쿠폰 적용 범위를 나타내는 Value Object
 * 쿠폰이 어떤 상품/카테고리에 적용 가능한지 정의합니다.
 */
public record CouponScope(
    ScopeType scopeType,
    Long scopeId
) {

    public CouponScope {
        validate(scopeType, scopeId);
    }

    /**
     * 전체 상품에 적용 가능한 범위를 생성합니다
     */
    public static CouponScope forAllProducts() {
        return new CouponScope(ScopeType.ALL, null);
    }

    /**
     * 특정 카테고리에 적용 가능한 범위를 생성합니다
     */
    public static CouponScope forCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Category ID must not be null for category scope"
            );
        }
        return new CouponScope(ScopeType.CATEGORY, categoryId);
    }

    /**
     * 특정 상품에 적용 가능한 범위를 생성합니다
     */
    public static CouponScope forProduct(Long productId) {
        if (productId == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Product ID must not be null for product scope"
            );
        }
        return new CouponScope(ScopeType.PRODUCT, productId);
    }

    /**
     * 전체 상품 적용 범위인지 확인합니다
     */
    public boolean isForAllProducts() {
        return scopeType == ScopeType.ALL;
    }

    /**
     * 카테고리 적용 범위인지 확인합니다
     */
    public boolean isForCategory() {
        return scopeType == ScopeType.CATEGORY;
    }

    /**
     * 상품 적용 범위인지 확인합니다
     */
    public boolean isForProduct() {
        return scopeType == ScopeType.PRODUCT;
    }

    private static void validate(ScopeType scopeType, Long scopeId) {
        if (scopeType == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Scope type must not be null"
            );
        }

        // ALL 타입의 경우 scopeId는 null이어야 함
        if (scopeType == ScopeType.ALL && scopeId != null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Scope ID must be null when scope type is ALL"
            );
        }

        // CATEGORY 또는 PRODUCT 타입의 경우 scopeId가 필수
        if ((scopeType == ScopeType.CATEGORY || scopeType == ScopeType.PRODUCT) && scopeId == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Scope ID must not be null when scope type is CATEGORY or PRODUCT"
            );
        }
    }
}
