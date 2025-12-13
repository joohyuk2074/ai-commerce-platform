package com.spartaecommerce.coupon.domain.port.in;

import com.spartaecommerce.coupon.application.dto.command.RegisterCouponCommand;
import com.spartaecommerce.coupon.application.dto.result.CouponUserResult;

/**
 * 오프라인 쿠폰 등록 유스케이스
 */
public interface RegisterCouponUseCase {

    /**
     * 오프라인 쿠폰을 사용자에게 등록합니다
     *
     * @param command 등록 커맨드
     * @return 등록된 쿠폰 정보
     */
    CouponUserResult register(RegisterCouponCommand command);
}
