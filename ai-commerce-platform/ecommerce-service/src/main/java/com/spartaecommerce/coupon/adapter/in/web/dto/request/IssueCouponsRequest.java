package com.spartaecommerce.coupon.adapter.in.web.dto.request;

import com.spartaecommerce.coupon.application.dto.command.IssueCouponsCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 쿠폰 대량 발급 요청
 */
public record IssueCouponsRequest(
        @NotNull(message = "쿠폰 ID는 필수입니다.")
        Long couponId,

        @NotNull(message = "발급 수량은 필수입니다.")
        @Min(value = 1, message = "발급 수량은 최소 1개 이상이어야 합니다.")
        @Max(value = 10000, message = "발급 수량은 최대 10,000개까지 가능합니다.")
        Integer quantity
) {
    public IssueCouponsCommand toCommand() {
        return new IssueCouponsCommand(couponId, quantity);
    }
}
