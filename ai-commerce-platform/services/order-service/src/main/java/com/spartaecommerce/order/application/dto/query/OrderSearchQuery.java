package com.spartaecommerce.order.application.dto.query;

import com.spartaecommerce.api.pagination.CustomPageable;
import com.spartaecommerce.domain.vo.DateRange;
import com.spartaecommerce.exception.BusinessException;
import com.spartaecommerce.exception.ErrorCode;
import com.spartaecommerce.order.domain.entity.OrderStatus;

public record OrderSearchQuery(
    Long userId,
    OrderStatus orderStatus,
    DateRange dateRange,
    CustomPageable pageable
) {
    public static OrderSearchQuery of(
        Long userId,
        OrderStatus orderStatus,
        DateRange dateRange,
        Integer page,
        Integer size,
        String sortBy,
        String direction
    ) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "userId cannot be null");
        }

        CustomPageable customPageable = CustomPageable.of(page, size, sortBy, direction);
        return new OrderSearchQuery(userId, orderStatus, dateRange, customPageable);
    }
}