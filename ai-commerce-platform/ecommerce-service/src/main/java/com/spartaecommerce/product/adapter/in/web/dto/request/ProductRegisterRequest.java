package com.spartaecommerce.product.adapter.in.web.dto.request;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.application.dto.command.ProductRegisterCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "상품 등록 요청")
public record ProductRegisterRequest(
    @Schema(description = "상품명", example = "스마트폰", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 100)
    String name,

    @Schema(description = "상품 설명", example = "최신 스마트폰입니다.")
    String description,

    @Schema(description = "상품 가격", example = "1000000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @DecimalMin(value = "0.0")
    BigDecimal price,

    @Schema(description = "재고 수량", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    Integer stock,

    @Schema(description = "카테고리 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    Long categoryId
) {

    public ProductRegisterCommand toCommand() {
        return new ProductRegisterCommand(
            name, description, Money.from(price), stock, categoryId
        );
    }
}
