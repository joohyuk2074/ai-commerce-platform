package com.spartaecommerce.order.domain.entity;

import com.spartaecommerce.common.util.DateTimeHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderHistory 도메인 엔티티")
class OrderHistoryTest {

    @Nested
    @DisplayName("주문 히스토리 생성 시")
    class CreateNew {

        @Test
        @DisplayName("유효한 정보로 히스토리를 생성한다")
        void createNew_WithValidData_CreatesHistory() {
            // given
            Long orderId = 1L;
            OrderStatus fromStatus = OrderStatus.PENDING;
            OrderStatus toStatus = OrderStatus.COMPLETED;
            String reason = "주문 완료";
            LocalDateTime now = LocalDateTime.now();
            DateTimeHolder dateTimeHolder = () -> now;

            // when
            OrderHistory history = OrderHistory.createNew(
                orderId,
                fromStatus,
                toStatus,
                reason,
                dateTimeHolder
            );

            // then
            assertThat(history.getOrderId()).isEqualTo(orderId);
            assertThat(history.getFromStatus()).isEqualTo(fromStatus);
            assertThat(history.getToStatus()).isEqualTo(toStatus);
            assertThat(history.getReason()).isEqualTo(reason);
            assertThat(history.getChangedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("최초 생성 시 fromStatus가 null일 수 있다")
        void createNew_WithNullFromStatus_CreatesHistory() {
            // given
            Long orderId = 1L;
            OrderStatus toStatus = OrderStatus.PENDING;
            String reason = "최초 주문 생성";
            LocalDateTime now = LocalDateTime.now();
            DateTimeHolder dateTimeHolder = () -> now;

            // when
            OrderHistory history = OrderHistory.createNew(
                orderId,
                null,
                toStatus,
                reason,
                dateTimeHolder
            );

            // then
            assertThat(history.getFromStatus()).isNull();
            assertThat(history.getToStatus()).isEqualTo(toStatus);
            assertThat(history.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("취소 히스토리를 생성한다")
        void createNew_ForCancellation_CreatesHistory() {
            // given
            Long orderId = 1L;
            OrderStatus fromStatus = OrderStatus.PENDING;
            OrderStatus toStatus = OrderStatus.CANCELED;
            String reason = "주문 취소";
            LocalDateTime now = LocalDateTime.now();
            DateTimeHolder dateTimeHolder = () -> now;

            // when
            OrderHistory history = OrderHistory.createNew(
                orderId,
                fromStatus,
                toStatus,
                reason,
                dateTimeHolder
            );

            // then
            assertThat(history.getFromStatus()).isEqualTo(fromStatus);
            assertThat(history.getToStatus()).isEqualTo(toStatus);
            assertThat(history.getReason()).isEqualTo(reason);
        }
    }
}
