package com.spartaecommerce.coupon.application.dto.query;

import com.spartaecommerce.common.domain.CustomPageable;

import java.time.LocalDateTime;

public record CouponSearchQuery(
    Boolean isActive,
    LocalDateTime now,
    CustomPageable pageable
) {
}
