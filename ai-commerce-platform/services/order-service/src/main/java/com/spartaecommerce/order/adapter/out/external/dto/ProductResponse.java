package com.spartaecommerce.order.adapter.out.external.dto;

import com.spartaecommerce.domain.vo.Money;
import com.spartaecommerce.order.domain.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long productId,
    String name,
    BigDecimal price,
    Integer stock,
    Long categoryId,
    Long sellerId,
    boolean isOrderable,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

  public Product toDomain() {
    return new Product(
        productId,
        name,
        "",
        Money.from(price),
        stock,
        categoryId,
        sellerId,
        isOrderable,
        createdAt,
        updatedAt
    );
  }
}
