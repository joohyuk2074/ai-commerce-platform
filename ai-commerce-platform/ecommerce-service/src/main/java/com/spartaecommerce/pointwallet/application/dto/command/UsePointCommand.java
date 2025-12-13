package com.spartaecommerce.pointwallet.application.dto.command;

import com.spartaecommerce.common.domain.Money;

public record UsePointCommand(
    Long userId,
    Money amount,
    Integer orderAmount,  // 주문 금액 (최소 사용 금액 검증용)
    String description
) {
}