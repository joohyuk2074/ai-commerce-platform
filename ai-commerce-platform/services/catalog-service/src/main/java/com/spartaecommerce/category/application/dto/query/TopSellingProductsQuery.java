package com.spartaecommerce.category.application.dto.query;

import java.time.LocalDate;

public record TopSellingProductsQuery(
    LocalDate startDate,
    LocalDate endDate,
    Integer limit
) {
}
