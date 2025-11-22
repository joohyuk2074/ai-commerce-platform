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

            User user = createUser();
            Long userId = userRepository.save(user);

            Product product = createProduct("리팩터링", initialStock - orderQuantity);
            Long productId = productRepository.save(product);

            Order order = createOrder(userId, productId, orderQuantity);
            Long orderId = orderRepository.save(order);

            OrderHistory newCreateHistory = createOrderHistory(orderId, null, OrderStatus.PENDING);
            orderHistoryRepository.save(newCreateHistory);

            // when
            sut.cancel(orderId);

            // then
            Order canceledOrder = orderRepository.getById(orderId);
            assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);

            Product restoredProduct = productRepository.getById(productId);
            assertThat(restoredProduct.getStock()).isEqualTo(initialStock);

            // 취소 히스토리 확인
            List<OrderHistory> histories = orderHistoryRepository.findByOrderId(orderId);
            OrderHistory orderHistory = histories.getFirst();
            assertThat(orderHistory.getToStatus()).isEqualTo(OrderStatus.CANCELED);
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
