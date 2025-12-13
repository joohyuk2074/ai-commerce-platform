package com.spartaecommerce.order.domain.entity;

import com.spartaecommerce.common.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderItem 도메인 엔티티")
class OrderItemTest {

    @Nested
    @DisplayName("주문 아이템 생성 시")
    class CreateNew {

        @Test
        @DisplayName("유효한 정보로 주문 아이템을 생성한다")
        void createNew_WithValidData_CreatesOrderItem() {
            // given
            Long productId = 1L;
            String productName = "클린코드";
            Money price = Money.from(new BigDecimal("30000"));
            Integer quantity = 2;

            // when
            OrderItem orderItem = OrderItem.createNew(productId, productName, price, quantity);

            // then
            assertThat(orderItem.getProductId()).isEqualTo(productId);
            assertThat(orderItem.getProductName()).isEqualTo(productName);
            assertThat(orderItem.getProductPrice()).isEqualTo(price);
            assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        }
    }

    @Nested
    @DisplayName("총 가격 계산 시")
    class GetTotalPrice {

        @Test
        @DisplayName("단가 * 수량을 정확히 계산한다")
        void getTotalPrice_CalculatesCorrectly() {
            // given
            OrderItem orderItem = OrderItem.createNew(
                1L,
                "상품",
                Money.from(new BigDecimal("15000")),
                3
            );

            // when
            Money totalPrice = orderItem.getTotalPrice();

            // then
            assertThat(totalPrice.amount()).isEqualByComparingTo(new BigDecimal("45000"));
        }

        @Test
        @DisplayName("수량이 1이면 단가와 같다")
        void getTotalPrice_WithQuantityOne_EqualToUnitPrice() {
            // given
            Money unitPrice = Money.from(new BigDecimal("10000"));
            OrderItem orderItem = OrderItem.createNew(1L, "상품", unitPrice, 1);

            // when
            Money totalPrice = orderItem.getTotalPrice();

            // then
            assertThat(totalPrice).isEqualTo(unitPrice);
        }

        @Test
        @DisplayName("소수점 가격도 정확히 계산한다")
        void getTotalPrice_WithDecimalPrice_CalculatesCorrectly() {
            // given
            OrderItem orderItem = OrderItem.createNew(
                1L,
                "상품",
                Money.from(new BigDecimal("9.99")),
                3
            );

            // when
            Money totalPrice = orderItem.getTotalPrice();

            // then
            assertThat(totalPrice.amount()).isEqualByComparingTo(new BigDecimal("29.97"));
        }
    }
}
