package com.spartaecommerce.pointwallet.adapter.in.web.dto.request;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.pointwallet.application.dto.command.UsePointCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UsePointRequest(
    @NotNull
    Long userId,

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal amount,

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal orderAmount,

    String description
) {

    public UsePointCommand toCommand() {
        return new UsePointCommand(
            userId,
            Money.from(amount),
            orderAmount.intValue(),
            description != null ? description : "Point used"
        );
    }
}
