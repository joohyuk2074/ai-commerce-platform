package com.spartaecommerce.order.application;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderHistory;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderCancelServiceTest extends OrderServiceTest {

    @Nested
    @DisplayName("주문 취소 요청시")
    class Cancel {

        @Test
        @DisplayName("성공적으로 취소하고 상품 재고를 복구한다")
        void cancel_ValidOrder_CancelsOrderAndRestoresStock() {
            // given
            int initialStock = 100;
            int orderQuantity = 30;

            Long userId = createUserWithWallet();

            Product product = createProduct("리팩터링", initialStock);
            Long productId = productRepository.save(product);

            // 실제로 주문 생성 (재고 차감됨)
            Long orderId = sut.create(new com.spartaecommerce.order.application.dto.command.OrderCreateCommand(
                userId,
                List.of(new com.spartaecommerce.order.application.dto.command.OrderItemCreateCommand(productId, orderQuantity)),
                "서울시",
                java.math.BigDecimal.ZERO
            ));

            // 주문 후 재고 확인
            Product afterOrder = productRepository.getById(productId);
            assertThat(afterOrder.getStock()).isEqualTo(initialStock - orderQuantity);

            // when
            sut.cancel(orderId);

            // then
            Order canceledOrder = orderRepository.getById(orderId);
            assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);

            Product restoredProduct = productRepository.getById(productId);
            assertThat(restoredProduct.getStock()).isEqualTo(initialStock);

            // 취소 히스토리 확인
            List<OrderHistory> histories = orderHistoryRepository.findByOrderId(orderId);
            boolean hasCancelHistory = histories.stream()
                .anyMatch(h -> h.getToStatus() == OrderStatus.CANCELED);
            assertThat(hasCancelHistory).isTrue();
        }

        @Test
        @DisplayName("이미 취소된 주문을 다시 취소하면 예외가 발생한다")
        void cancel_AlreadyCanceledOrder_ThrowsException() {
            // given
            Order order = createOrder(1L, 1L ,10);
            order.cancel();
            Long orderId = orderRepository.save(order);

            // when & then
            assertThatThrownBy(() -> sut.cancel(orderId))
                .isInstanceOf(BusinessException.class);
        }
    }

}
