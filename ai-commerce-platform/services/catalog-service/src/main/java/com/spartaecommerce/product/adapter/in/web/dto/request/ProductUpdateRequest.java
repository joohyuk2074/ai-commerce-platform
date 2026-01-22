package com.spartaecommerce.product.adapter.in.web.dto.request;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "상품 수정 요청 (모든 필드는 선택사항이며, 입력된 필드만 수정됩니다)")
public record ProductUpdateRequest(
    @Schema(description = "상품명", example = "스마트폰 Pro")
    String name,

    @Schema(description = "상품 설명", example = "업그레이드된 최신 스마트폰입니다.")
    String description,

    @Schema(description = "상품 가격", example = "1200000")
    BigDecimal price,

    @Schema(description = "재고 수량", example = "50")
    Integer stock,

    @Schema(description = "카테고리 ID", example = "1")
    Long categoryId
) {
    public ProductUpdateCommand toCommand(Long productId) {
        return new ProductUpdateCommand(
            productId,
            name,
            description,
            Money.from(price),
            stock,
            categoryId
        );
    }
}
