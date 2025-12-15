package com.spartaecommerce.order.application;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderItemCreateCommand;
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

public class OrderCreateServiceTest extends OrderServiceTest {

    @Nested
    @DisplayName("주문 생성 요청시")
    class Create {

        @Test
        @DisplayName("유효한 요청으로 주문을 생성하고 재고를 차감한다")
        void createOrder_WithValidRequest_CreatesOrderAndDeductsStock() {
            // given
            int initialStock = 50;
            int orderQuantity = 5;

            Long userId = createUserWithWallet();

            Product product = createProduct("클린코드", initialStock);
            Long productId = productRepository.save(product);

            OrderCreateCommand command = new OrderCreateCommand(
                userId,
                List.of(new OrderItemCreateCommand(productId, orderQuantity)),
                "서울시 강남구 테헤란로 123",
                BigDecimal.ZERO
            );

            // when
            Long orderId = sut.create(command);

            // then
            assertThat(orderId).isNotNull();

            // 주문이 생성되었는지 확인
            Order savedOrder = orderRepository.getById(orderId);
            assertThat(savedOrder.getUserId()).isEqualTo(userId);
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(savedOrder.getOrderItems().size()).isEqualTo(1);
            assertThat(savedOrder.getOrderItems().getFirst().getQuantity()).isEqualTo(orderQuantity);

            // 재고가 차감되었는지 확인
            Product updatedProduct = productRepository.getById(productId);
            assertThat(updatedProduct.getStock()).isEqualTo(initialStock - orderQuantity);

            // 주문 히스토리가 생성되었는지 확인
            List<OrderHistory> histories = orderHistoryRepository.findByOrderId(orderId);
            assertThat(histories.size()).isEqualTo(1);
            assertThat(histories.getFirst().getToStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 요청인 경우 예외가 발생한다")
        void createOrder_WithNonExistentUser_ThrowsException() {
            // given
            Long nonExistentUserId = 999L;

            OrderCreateCommand command = new OrderCreateCommand(
                nonExistentUserId,
                List.of(new OrderItemCreateCommand(1L, 5)),
                "서울시 강남구",
                BigDecimal.ZERO
            );

            // when & then
            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("재고가 부족한 경우 예외가 발생하고 주문이 생성되지 않는다")
        void createOrder_WithInsufficientStock_ThrowsExceptionAndDoesNotCreateOrder() {
            // given
            int initialStock = 3;
            int requestedQuantity = 5;

            Long userId = createUserWithWallet();

            Product product = createProduct("클린코드", initialStock);
            Long productId = productRepository.save(product);

            OrderCreateCommand command = new OrderCreateCommand(
                userId,
                List.of(new OrderItemCreateCommand(productId, requestedQuantity)),
                "서울시 강남구",
                BigDecimal.ZERO
            );

            // when & then
            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Available: 3, Requested: 5");

            Product unchangedProduct = productRepository.getById(productId);
            assertThat(unchangedProduct.getStock()).isEqualTo(initialStock);
        }

        @Test
        @DisplayName("여러 주문이 생성되면 각각 재고가 정확히 차감된다")
        void createOrder_MultipleOrders_DeductsStockCorrectly() {
            // given
            int initialStock = 100;

            Long user1Id = createUserWithWallet();
            Long user2Id = createUserWithWallet();

            Product product = createProduct("클린코드", initialStock);
            Long productId = productRepository.save(product);

            // when
            sut.create(new OrderCreateCommand(user1Id, List.of(new OrderItemCreateCommand(productId, 10)), "주소1", BigDecimal.ZERO));
            sut.create(new OrderCreateCommand(user2Id, List.of(new OrderItemCreateCommand(productId, 20)), "주소2", BigDecimal.ZERO));
            sut.create(new OrderCreateCommand(user1Id, List.of(new OrderItemCreateCommand(productId, 15)), "주소3", BigDecimal.ZERO));

            // then
            Product finalProduct = productRepository.getById(productId);
            assertThat(finalProduct.getStock()).isEqualTo(100 - 10 - 20 - 15);
        }
    }

}
