package com.spartaecommerce.product.adapter.in.web.dto.response;

import com.spartaecommerce.product.application.dto.result.ProductResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "상품 응답")
public record ProductResponse(
    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "스마트폰")
    String name,

    @Schema(description = "상품 가격", example = "1000000")
    BigDecimal price,

    @Schema(description = "재고 수량", example = "100")
    Integer stock,

    @Schema(description = "카테고리 ID", example = "1")
    Long categoryId,

    @Schema(description = "판매자 Id", example = "1")
    Long sellerId,

    @Schema(description = "주문 가능 여부", example = "true")
    boolean isOrderable,

    @Schema(description = "생성 일시", example = "2024-01-01T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 일시", example = "2024-01-01T00:00:00")
    LocalDateTime updatedAt
) {

  public static ProductResponse from(ProductResult productResult) {
    return new ProductResponse(
        productResult.productId(),
        productResult.name(),
        productResult.price().amount(),
        productResult.stock(),
        productResult.categoryId(),
        productResult.sellerId(),
        productResult.isOrderable(),
        productResult.createdAt(),
        productResult.updatedAt()
    );
  }
}
