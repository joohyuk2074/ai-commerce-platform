package com.spartaecommerce.order.domain.entity;

import com.spartaecommerce.util.DateTimeHolder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderHistory {

    private Long orderHistoryId;

    private Long orderId;

    private OrderStatus fromStatus;

    private OrderStatus toStatus;

    private String reason;

    private LocalDateTime changedAt;

    public static OrderHistory createNew(
        Long orderId,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String reason,
        DateTimeHolder dateTimeHolder
    ) {
        return OrderHistory.builder()
            .orderId(orderId)
            .fromStatus(fromStatus)
            .toStatus(toStatus)
            .reason(reason)
            .changedAt(dateTimeHolder.getCurrentDateTime())
            .build();
    }
}
