package com.spartaecommerce.category.adapter.in.web.dto.response;

import com.spartaecommerce.category.application.dto.result.CategoryProductSalesRankingResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryProductSalesRankingResponse {

    private List<CategorySalesRanking> categories;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySalesRanking {
        private Long categoryId;
        private String categoryName;
        private List<ProductSalesInfo> topProducts;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSalesInfo {
        private Integer rank;
        private Long productId;
        private String productName;
        private Long totalSalesQuantity;
    }

    /**
     * Application Layer의 Result를 Presentation Layer의 Response로 변환
     */
    public static CategoryProductSalesRankingResponse from(List<CategoryProductSalesRankingResult> results) {
        List<CategorySalesRanking> categories = results.stream()
            .map(CategoryProductSalesRankingResponse::toCategorySalesRanking)
            .toList();

        return CategoryProductSalesRankingResponse.builder()
            .categories(categories)
            .build();
    }

    private static CategorySalesRanking toCategorySalesRanking(CategoryProductSalesRankingResult result) {
        List<ProductSalesInfo> topProducts = result.getTopProducts().stream()
            .map(CategoryProductSalesRankingResponse::toProductSalesInfo)
            .toList();

        return CategorySalesRanking.builder()
            .categoryId(result.getCategoryId())
            .categoryName(result.getCategoryName())
            .topProducts(topProducts)
            .build();
    }

    private static ProductSalesInfo toProductSalesInfo(CategoryProductSalesRankingResult.ProductRankInfo rankInfo) {
        return ProductSalesInfo.builder()
            .rank(rankInfo.getRank())
            .productId(rankInfo.getProductId())
            .productName(rankInfo.getProductName())
            .totalSalesQuantity(rankInfo.getTotalSalesQuantity())
            .build();
    }
}
