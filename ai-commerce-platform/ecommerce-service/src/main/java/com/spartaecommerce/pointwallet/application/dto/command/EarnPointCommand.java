package com.spartaecommerce.pointwallet.application.dto.command;

import com.spartaecommerce.common.domain.Money;

public record EarnPointCommand(
    Long userId,
    Money amount,
    String description
) {
}