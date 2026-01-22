package com.spartaecommerce.pointwallet.adapter.in.web.internal.dto;

import java.math.BigDecimal;

public record PointCalculationResponse(
    BigDecimal expectedPoints
) {
}
