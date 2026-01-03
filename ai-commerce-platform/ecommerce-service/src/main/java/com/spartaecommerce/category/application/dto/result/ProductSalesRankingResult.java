package com.spartaecommerce.category.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSalesRankingResult {

    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Long totalSalesQuantity;
}
