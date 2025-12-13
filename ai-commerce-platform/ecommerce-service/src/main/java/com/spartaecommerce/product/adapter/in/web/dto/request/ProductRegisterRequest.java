package com.spartaecommerce.product.adapter.in.web.dto.request;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.application.dto.command.ProductRegisterCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRegisterRequest(
    @NotBlank
    @Size(min = 1, max = 100)
    String name,

    String description,

    @NotNull
    @DecimalMin(value = "0.0")
    BigDecimal price,

    @NotNull
    Integer stock,

    @NotNull
    Long categoryId
) {

    public ProductRegisterCommand toCommand() {
        return new ProductRegisterCommand(
            name, description, Money.from(price), stock, categoryId
        );
    }
}
