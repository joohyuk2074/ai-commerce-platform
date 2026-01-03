package com.spartaecommerce.category.adapter.in.web.dto.request;

import com.spartaecommerce.category.application.dto.query.TopSellingProductsQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TopSellingProductsRequest(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate,

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit must not exceed 100")
    Integer limit
) {
    public TopSellingProductsRequest {
        // 기본값: 최근 30일
        if (startDate == null && endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        } else if (startDate == null) {
            startDate = endDate.minusDays(30);
        } else if (endDate == null) {
            endDate = LocalDate.now();
        }

        if (limit == null) {
            limit = 10;
        }
    }

    public TopSellingProductsQuery toQuery() {
        return new TopSellingProductsQuery(startDate, endDate, limit);
    }
}
