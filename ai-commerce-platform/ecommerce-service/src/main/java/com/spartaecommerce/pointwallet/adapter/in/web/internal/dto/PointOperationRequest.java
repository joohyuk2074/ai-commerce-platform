package com.spartaecommerce.pointwallet.adapter.in.web.internal.dto;

import java.math.BigDecimal;

public record PointOperationRequest(
    BigDecimal amount,
    String description
) {
}
