package com.spartaecommerce.product.adapter.in.web.dto.request;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

@Schema(description = "상품 검색 요청")
public record ProductSearchRequest(
    @Schema(description = "카테고리 ID", example = "1")
    Long categoryId,

    @Schema(description = "주문 가능 여부", example = "true")
    Boolean isOrderable,

    @Schema(description = "최소 가격", example = "10000")
    BigDecimal minPrice,

    @Schema(description = "최대 가격", example = "100000")
    BigDecimal maxPrice,

    @Schema(description = "검색 키워드 (상품명 검색)", example = "스마트폰")
    String keyword,

    @Schema(description = "페이지 번호 (1부터 시작, 기본값: 1)", example = "1")
    Integer page,

    @Schema(description = "페이지 크기 (최대 100, 기본값: 20)", example = "20")
    Integer size,

    @Schema(description = "정렬 기준 필드 (기본값: createdAt)", example = "createdAt", allowableValues = {"createdAt", "price", "name"})
    String sortBy,

    @Schema(description = "정렬 방향 (기본값: DESC)", example = "DESC", allowableValues = {"ASC", "DESC"})
    String direction
) {

    public ProductSearchQuery toQuery() {
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (size != null && size > 0 && size <= 100) ? size : 20;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        String sortDirection = (direction != null && !direction.isBlank()) ? direction : "DESC";

        Sort.Direction dir = sortDirection.equalsIgnoreCase("ASC")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(dir, sortField));

        return new ProductSearchQuery(
            categoryId,
            isOrderable,
            Money.from(minPrice),
            Money.from(maxPrice),
            keyword,
            pageable
        );
    }
}
