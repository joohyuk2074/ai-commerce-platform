package com.spartaecommerce.adapter.out.external.dto;

public record ProductStockRequest(
    Long productId,
    Integer quantity
) {
}
