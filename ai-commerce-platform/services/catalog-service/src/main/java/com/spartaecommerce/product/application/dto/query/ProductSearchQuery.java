package com.spartaecommerce.product.application.dto.query;

import com.spartaecommerce.domain.vo.Money;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ProductSearchQuery(
    Long categoryId,
    Boolean isOrderable,
    Money minPrice,
    Money maxPrice,
    String keyword,
    Pageable pageable
) {

  public ProductSearchQuery {
    if (pageable == null) {
      pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
  }
}