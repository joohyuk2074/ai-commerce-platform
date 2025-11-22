package com.spartaecommerce.pointwallet.controller.dto.request;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.pointwallet.domain.command.EarnPointCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EarnPointRequest(
    @NotNull
    Long userId,

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal amount,

    String description
) {

    public EarnPointCommand toCommand() {
        return new EarnPointCommand(
            userId,
            Money.from(amount),
            description != null ? description : "Point earned"
        );
    }
}