package com.spartaecommerce.order.adapter.in.web.dto.request;

import com.spartaecommerce.domain.vo.DateRange;
import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderSearchRequest(
    @NotNull
    Long userId,
    OrderStatus orderStatus,
    DateRange dateRange,
    Integer page,
    Integer size,
    String sortBy,
    String direction
) {

  public OrderSearchQuery toQuery() {
    return OrderSearchQuery.of(
        userId,
        orderStatus,
        dateRange,
        page,
        size,
        sortBy,
        direction
    );
  }
}
