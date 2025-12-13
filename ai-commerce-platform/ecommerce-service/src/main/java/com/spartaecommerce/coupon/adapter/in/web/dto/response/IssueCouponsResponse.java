package com.spartaecommerce.coupon.adapter.in.web.dto.response;

import java.util.List;

/**
 * 쿠폰 대량 발급 응답
 */
public record IssueCouponsResponse(
        Integer issuedCount,
        List<Long> couponUserIds
) {
    public static IssueCouponsResponse of(List<Long> couponUserIds) {
        return new IssueCouponsResponse(couponUserIds.size(), couponUserIds);
    }
}
