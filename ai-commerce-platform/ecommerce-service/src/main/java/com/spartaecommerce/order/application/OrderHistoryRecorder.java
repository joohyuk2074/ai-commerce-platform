package com.spartaecommerce.order.application;

import com.spartaecommerce.common.util.DateTimeHolder;
import com.spartaecommerce.order.domain.entity.OrderHistory;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.domain.port.out.SaveOrderHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 주문 히스토리 기록 도메인 서비스
 * - 주문 상태 변경 히스토리 기록
 */
@Service
@RequiredArgsConstructor
public class OrderHistoryRecorder {

    private final SaveOrderHistoryPort saveOrderHistoryPort;
    private final DateTimeHolder dateTimeHolder;

    /**
     * 주문 생성 히스토리 기록
     */
    public void recordCreation(Long orderId) {
        OrderHistory history = OrderHistory.createNew(
            orderId,
            null,
            OrderStatus.PENDING,
            "최초 주문 생성",
            dateTimeHolder
        );
        saveOrderHistoryPort.save(history);
    }

    /**
     * 주문 상태 변경 히스토리 기록
     */
    public void recordStatusChange(Long orderId, OrderStatus previousStatus, OrderStatus newStatus, String reason) {
        OrderHistory history = OrderHistory.createNew(
            orderId,
            previousStatus,
            newStatus,
            reason,
            dateTimeHolder
        );
        saveOrderHistoryPort.save(history);
    }

    /**
     * 주문 취소 히스토리 기록
     */
    public void recordCancellation(Long orderId, OrderStatus previousStatus) {
        recordStatusChange(orderId, previousStatus, OrderStatus.CANCELED, "주문 취소");
    }
}