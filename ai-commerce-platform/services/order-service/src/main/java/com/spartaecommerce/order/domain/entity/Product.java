package com.spartaecommerce.order.domain.entity;

import com.spartaecommerce.domain.vo.Money;
import java.time.LocalDateTime;

public record Product(
    Long productId,
    String name,
    String description,
    Money price,
    Integer stock,
    Long categoryId,
    Long sellerId,
    boolean isOrderable,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
