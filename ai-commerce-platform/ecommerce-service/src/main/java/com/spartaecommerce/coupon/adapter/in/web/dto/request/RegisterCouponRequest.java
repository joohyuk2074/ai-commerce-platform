package com.spartaecommerce.coupon.adapter.in.web.dto.request;

import com.spartaecommerce.coupon.application.dto.command.RegisterCouponCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 쿠폰 등록 요청
 */
public record RegisterCouponRequest(
        @NotBlank(message = "쿠폰 코드는 필수입니다.")
        String couponCode,

        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId
) {
    public RegisterCouponCommand toCommand() {
        return new RegisterCouponCommand(couponCode, userId);
    }
}
