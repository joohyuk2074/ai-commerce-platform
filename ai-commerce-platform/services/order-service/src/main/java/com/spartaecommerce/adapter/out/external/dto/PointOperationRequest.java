package com.spartaecommerce.adapter.out.external.dto;

import java.math.BigDecimal;

public record PointOperationRequest(
    BigDecimal amount,
    String description
) {
}
