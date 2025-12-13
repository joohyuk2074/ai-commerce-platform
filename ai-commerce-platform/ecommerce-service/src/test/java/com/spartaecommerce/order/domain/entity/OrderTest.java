package com.spartaecommerce.order.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order 도메인 엔티티")
class OrderTest {

    @Nested
    @DisplayName("주문 생성 시")
    class CreateNew {

        @Test
        @DisplayName("유효한 정보로 주문을 생성한다")
        void createNew_WithValidData_CreatesOrder() {
            // given
            Long userId = 1L;
            String shippingAddress = "서울시 강남구 테헤란로 1";

            // when
            Order order = Order.createNew(userId, shippingAddress);

            // then
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getShippingAddress()).isEqualTo(shippingAddress);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getOrderItems()).isEmpty();
            assertThat(order.getUsedPointAmount()).isEqualTo(Money.zero());
            assertThat(order.getEarnedPointAmount()).isEqualTo(Money.zero());
        }
    }

    @Nested
    @DisplayName("주문 아이템 추가 시")
    class AddOrderItem {

        @Test
        @DisplayName("유효한 상품 정보로 주문 아이템을 추가한다")
        void addOrderItem_WithValidData_AddsItem() {
            // given
            Order order = createOrder();
            Long productId = 1L;
            String productName = "클린코드";
            Money price = Money.from(new BigDecimal("30000"));
            Integer quantity = 2;

            // when
            order.addOrderItem(productId, productName, price, quantity);

            // then
            assertThat(order.getOrderItems()).hasSize(1);
            OrderItem addedItem = order.getOrderItems().get(0);
            assertThat(addedItem.getProductId()).isEqualTo(productId);
            assertThat(addedItem.getProductName()).isEqualTo(productName);
            assertThat(addedItem.getProductPrice()).isEqualTo(price);
            assertThat(addedItem.getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("여러 상품을 추가하면 모두 저장된다")
        void addOrderItem_MultipleItems_AllAdded() {
            // given
            Order order = createOrder();

            // when
            order.addOrderItem(1L, "상품1", Money.from(new BigDecimal("10000")), 1);
            order.addOrderItem(2L, "상품2", Money.from(new BigDecimal("20000")), 2);
            order.addOrderItem(3L, "상품3", Money.from(new BigDecimal("30000")), 3);

            // then
            assertThat(order.getOrderItems()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("총 주문 금액 계산 시")
    class CalculateTotalAmount {

        @Test
        @DisplayName("모든 아이템의 총액을 정확히 계산한다")
        void calculateTotalAmount_WithMultipleItems_ReturnsCorrectTotal() {
            // given
            Order order = createOrder();
            order.addOrderItem(1L, "상품1", Money.from(new BigDecimal("10000")), 2); // 20000
            order.addOrderItem(2L, "상품2", Money.from(new BigDecimal("15000")), 3); // 45000

            // when
            BigDecimal total = order.calculateTotalAmount();

            // then
            assertThat(total).isEqualByComparingTo(new BigDecimal("65000"));
        }

        @Test
        @DisplayName("아이템이 없으면 0을 반환한다")
        void calculateTotalAmount_WithNoItems_ReturnsZero() {
            // given
            Order order = createOrder();

            // when
            BigDecimal total = order.calculateTotalAmount();

            // then
            assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("주문 상태 변경 시")
    class UpdateOrderStatus {

        @Test
        @DisplayName("PENDING에서 COMPLETED로 변경 가능하다")
        void updateOrderStatus_FromPendingToCompleted_Success() {
            // given
            Order order = createOrder();

            // when
            order.updateOrderStatus(OrderStatus.COMPLETED);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("PENDING에서 CANCELED로 변경 가능하다")
        void updateOrderStatus_FromPendingToCanceled_Success() {
            // given
            Order order = createOrder();

            // when
            order.updateOrderStatus(OrderStatus.CANCELED);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("같은 상태로 변경하면 아무 일도 일어나지 않는다")
        void updateOrderStatus_SameStatus_NoChange() {
            // given
            Order order = createOrder();

            // when & then (예외 발생하지 않음)
            order.updateOrderStatus(OrderStatus.PENDING);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("COMPLETED에서는 상태 변경이 불가능하다")
        void updateOrderStatus_FromCompleted_ThrowsException() {
            // given
            Order order = createOrder();
            order.updateOrderStatus(OrderStatus.COMPLETED);

            // when & then
            assertThatThrownBy(() -> order.updateOrderStatus(OrderStatus.CANCELED))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_INVALID_STATE_TRANSITION);
        }

        @Test
        @DisplayName("CANCELED에서는 상태 변경이 불가능하다")
        void updateOrderStatus_FromCanceled_ThrowsException() {
            // given
            Order order = createOrder();
            order.updateOrderStatus(OrderStatus.CANCELED);

            // when & then
            assertThatThrownBy(() -> order.updateOrderStatus(OrderStatus.COMPLETED))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_INVALID_STATE_TRANSITION);
        }
    }

    @Nested
    @DisplayName("주문 취소 시")
    class Cancel {

        @Test
        @DisplayName("PENDING 상태에서 취소 가능하다")
        void cancel_FromPending_Success() {
            // given
            Order order = createOrder();

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("이미 취소된 주문은 다시 취소할 수 없다")
        void cancel_AlreadyCanceled_ThrowsException() {
            // given
            Order order = createOrder();
            order.cancel();

            // when & then
            assertThatThrownBy(order::cancel)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_INVALID_STATE_TRANSITION)
                .hasMessageContaining("already been canceled");
        }

        @Test
        @DisplayName("COMPLETED 상태에서는 취소할 수 없다")
        void cancel_FromCompleted_ThrowsException() {
            // given
            Order order = createOrder();
            order.updateOrderStatus(OrderStatus.COMPLETED);

            // when & then
            assertThatThrownBy(order::cancel)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_INVALID_STATE_TRANSITION);
        }
    }

    @Nested
    @DisplayName("주문 완료 확인 시")
    class IsComplete {

        @Test
        @DisplayName("COMPLETED 상태이면 true를 반환한다")
        void isComplete_WhenCompleted_ReturnsTrue() {
            // given
            Order order = createOrder();
            order.updateOrderStatus(OrderStatus.COMPLETED);

            // when & then
            assertThat(order.isComplete()).isTrue();
        }

        @Test
        @DisplayName("PENDING 상태이면 false를 반환한다")
        void isComplete_WhenPending_ReturnsFalse() {
            // given
            Order order = createOrder();

            // when & then
            assertThat(order.isComplete()).isFalse();
        }

        @Test
        @DisplayName("CANCELED 상태이면 false를 반환한다")
        void isComplete_WhenCanceled_ReturnsFalse() {
            // given
            Order order = createOrder();
            order.cancel();

            // when & then
            assertThat(order.isComplete()).isFalse();
        }
    }

    @Nested
    @DisplayName("포인트 금액 설정 시")
    class SetPointAmounts {

        @Test
        @DisplayName("사용 포인트와 적립 포인트를 설정한다")
        void setPointAmounts_WithValidValues_SetsAmounts() {
            // given
            Order order = createOrder();
            Money usedPoints = Money.from(new BigDecimal("1000"));
            Money earnedPoints = Money.from(new BigDecimal("500"));

            // when
            order.setPointAmounts(usedPoints, earnedPoints);

            // then
            assertThat(order.getUsedPointAmount()).isEqualTo(usedPoints);
            assertThat(order.getEarnedPointAmount()).isEqualTo(earnedPoints);
        }
    }

    @Nested
    @DisplayName("주문 아이템 조회 시")
    class GetOrderItems {

        @Test
        @DisplayName("불변 리스트를 반환한다")
        void getOrderItems_ReturnsUnmodifiableList() {
            // given
            Order order = createOrder();
            order.addOrderItem(1L, "상품1", Money.from(new BigDecimal("10000")), 1);

            // when & then
            assertThatThrownBy(() ->
                order.getOrderItems().add(
                    OrderItem.createNew(2L, "상품2", Money.from(new BigDecimal("20000")), 1)
                )
            ).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // Helper methods
    private Order createOrder() {
        return Order.createNew(1L, "서울시 강남구 테헤란로 1");
    }
}
