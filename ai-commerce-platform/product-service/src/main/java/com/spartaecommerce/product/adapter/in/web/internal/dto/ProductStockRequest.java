package com.spartaecommerce.product.adapter.in.web.internal.dto;

public record ProductStockRequest(
    Long productId,
    Integer quantity
) {
}
