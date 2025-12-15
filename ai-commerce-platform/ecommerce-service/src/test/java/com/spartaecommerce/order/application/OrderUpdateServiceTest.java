package com.spartaecommerce.order.application;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderItemCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderStatusUpdateCommand;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderHistory;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OrderUpdateServiceTest extends OrderServiceTest {

    @Nested
    @DisplayName("주문 상태변경 요청시")
    class UpdateOrderStatus {

        @Test
        @DisplayName("PENDING 주문을 COMPLETED로 변경한다")
        void updateOrderStatus_FromPendingToCompleted_UpdatesStatusAndCreatesHistory() {
            // given
            Long userId = createUserWithWallet();
            Product product = createProduct("클린코드", 50);
            Long productId = productRepository.save(product);

            Long orderId = sut.create(new OrderCreateCommand(
                userId,
                List.of(new OrderItemCreateCommand(productId, 5)),
                "서울시",
                BigDecimal.ZERO
            ));

            OrderStatusUpdateCommand command = new OrderStatusUpdateCommand(
                orderId,
                OrderStatus.COMPLETED
            );

            // when
            sut.updateOrderStatus(command);

            // then
            Order updatedOrder = orderRepository.getById(orderId);
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);

            // 히스토리 확인
            List<OrderHistory> histories = orderHistoryRepository.findByOrderId(orderId);
            assertThat(histories.size()).isEqualTo(2);

            boolean hasCompletedHistory = histories.stream()
                .anyMatch(h -> h.getToStatus() == OrderStatus.COMPLETED);
            assertThat(hasCompletedHistory).isTrue();
        }

        @Test
        @DisplayName("CANCELED 상태로 변경 시 cancel 메서드가 호출되고 재고가 복구된다")
        void updateOrderStatus_ToCanceled_CallsCancelAndRestoresStock() {
            // given
            int initialStock = 50;
            int orderQuantity = 10;

            Long userId = createUserWithWallet();

            Product product = createProduct("클린코드", initialStock);
            Long productId = productRepository.save(product);

            Long orderId = sut.create(new OrderCreateCommand(
                userId,
                List.of(new OrderItemCreateCommand(productId, orderQuantity)),
                "서울시",
                BigDecimal.ZERO
            ));

            Product afterOrder = productRepository.getById(productId);
            assertThat(afterOrder.getStock()).isEqualTo(initialStock - orderQuantity);

            OrderStatusUpdateCommand command = new OrderStatusUpdateCommand(
                orderId,
                OrderStatus.CANCELED
            );

            // when
            sut.updateOrderStatus(command);

            // then
            Order canceledOrder = orderRepository.getById(orderId);
            assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);

            Product restoredProduct = productRepository.getById(productId);
            assertThat(restoredProduct.getStock()).isEqualTo(initialStock); // 원복
        }

        @Test
        @DisplayName("존재하지 않는 주문의 상태 변경 시 예외가 발생한다")
        void updateOrderStatus_WithNonExistentOrder_ThrowsException() {
            // given
            Long nonExistentOrderId = 999L;
            OrderStatusUpdateCommand command = new OrderStatusUpdateCommand(
                nonExistentOrderId,
                OrderStatus.COMPLETED
            );

            // when & then
            assertThatThrownBy(() -> sut.updateOrderStatus(command))
                .isInstanceOf(BusinessException.class);
        }
    }
}
