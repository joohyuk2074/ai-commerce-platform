package com.spartaecommerce.coupon.domain.port.in;

import com.spartaecommerce.coupon.application.dto.command.IssueCouponsCommand;

import java.util.List;

/**
 * 쿠폰 대량 발급 유스케이스
 */
public interface IssueCouponsUseCase {

    /**
     * 쿠폰을 대량으로 발급합니다
     *
     * @param command 발급 커맨드
     * @return 발급된 쿠폰 사용자 ID 목록
     */
    List<Long> issue(IssueCouponsCommand command);
}
