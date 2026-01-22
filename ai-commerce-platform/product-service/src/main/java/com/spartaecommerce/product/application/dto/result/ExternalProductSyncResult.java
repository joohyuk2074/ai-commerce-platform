package com.spartaecommerce.product.application.dto.result;

import java.math.BigDecimal;
import java.util.List;

public record ExternalProductSyncResult(
    List<ExternalProductItem> products,
    PageInfo pageInfo
) {
    public record ExternalProductItem(
        String vendor,
        Long externalId,
        String name,
        String description,
        BigDecimal price,
        Integer stock
    ) {}

    public record PageInfo(
        int pageNumber,
        int pageSize,
        int totalPages,
        long totalElements,
        boolean last
    ) {}
}
