package com.spartaecommerce.category.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryProductSalesRankingResult {

    private Long categoryId;
    private String categoryName;
    private List<ProductRankInfo> topProducts;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRankInfo {
        private Integer rank;
        private Long productId;
        private String productName;
        private Long totalSalesQuantity;
    }
}
