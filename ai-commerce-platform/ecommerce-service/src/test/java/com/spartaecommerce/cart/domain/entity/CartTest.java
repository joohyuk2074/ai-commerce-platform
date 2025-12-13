package com.spartaecommerce.cart.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cart 도메인 엔티티")
class CartTest {

    @Nested
    @DisplayName("장바구니 생성 시")
    class CreateNew {

        @Test
        @DisplayName("유효한 userId로 장바구니를 생성한다")
        void createNew_WithValidUserId_CreatesCart() {
            // given
            Long userId = 1L;

            // when
            Cart cart = Cart.createNew(userId);

            // then
            assertThat(cart.getUserId()).isEqualTo(userId);
            assertThat(cart.getItems()).isEmpty();
            assertThat(cart.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("userId가 null이면 예외가 발생한다")
        void createNew_WithNullUserId_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> Cart.createNew(null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }
    }

    @Nested
    @DisplayName("상품 추가 시")
    class AddItem {

        @Test
        @DisplayName("새로운 상품을 장바구니에 추가한다")
        void addItem_WithNewProduct_AddsSuccessfully() {
            // given
            Cart cart = Cart.createNew(1L);
            Long productId = 100L;
            Integer quantity = 2;
            Money price = Money.from(new BigDecimal("10000"));
            Integer maxItems = 10;

            // when
            cart.addItem(productId, quantity, price, maxItems);

            // then
            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getProductId()).isEqualTo(productId);
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(quantity);
            assertThat(cart.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("이미 존재하는 상품은 수량만 증가한다")
        void addItem_WithExistingProduct_IncreasesQuantity() {
            // given
            Cart cart = Cart.createNew(1L);
            Long productId = 100L;
            Money price = Money.from(new BigDecimal("10000"));
            Integer maxItems = 10;

            cart.addItem(productId, 2, price, maxItems);

            // when
            cart.addItem(productId, 3, price, maxItems);

            // then
            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("최대 상품 개수를 초과하면 예외가 발생한다")
        void addItem_ExceedsMaxItems_ThrowsException() {
            // given
            Cart cart = Cart.createNew(1L);
            Money price = Money.from(new BigDecimal("10000"));
            Integer maxItems = 2;

            cart.addItem(1L, 1, price, maxItems);
            cart.addItem(2L, 1, price, maxItems);

            // when & then
            assertThatThrownBy(() -> cart.addItem(3L, 1, price, maxItems))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }
    }

    @Nested
    @DisplayName("상품 제거 시")
    class RemoveItem {

        @Test
        @DisplayName("존재하는 상품을 제거한다")
        void removeItem_WithExistingProduct_RemovesSuccessfully() {
            // given
            Cart cart = Cart.createNew(1L);
            Long productId = 100L;
            Money price = Money.from(new BigDecimal("10000"));

            cart.addItem(productId, 2, price, 10);
            assertThat(cart.getItems()).hasSize(1);

            // when
            cart.removeItem(productId);

            // then
            assertThat(cart.getItems()).isEmpty();
            assertThat(cart.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 상품을 제거하면 예외가 발생한다")
        void removeItem_WithNonExistingProduct_ThrowsException() {
            // given
            Cart cart = Cart.createNew(1L);
            Long nonExistingProductId = 999L;

            // when & then
            assertThatThrownBy(() -> cart.removeItem(nonExistingProductId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상품 수량 변경 시")
    class UpdateItemQuantity {

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 100})
        @DisplayName("존재하는 상품의 수량을 변경한다")
        void updateItemQuantity_WithExistingProduct_UpdatesSuccessfully(int newQuantity) {
            // given
            Cart cart = Cart.createNew(1L);
            Long productId = 100L;
            Money price = Money.from(new BigDecimal("10000"));

            cart.addItem(productId, 2, price, 10);

            // when
            cart.updateItemQuantity(productId, newQuantity);

            // then
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(newQuantity);
        }

        @Test
        @DisplayName("존재하지 않는 상품의 수량 변경 시 예외가 발생한다")
        void updateItemQuantity_WithNonExistingProduct_ThrowsException() {
            // given
            Cart cart = Cart.createNew(1L);
            Long nonExistingProductId = 999L;

            // when & then
            assertThatThrownBy(() -> cart.updateItemQuantity(nonExistingProductId, 5))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("장바구니 비우기 시")
    class Clear {

        @Test
        @DisplayName("모든 상품을 제거한다")
        void clear_RemovesAllItems() {
            // given
            Cart cart = Cart.createNew(1L);
            Money price = Money.from(new BigDecimal("10000"));

            cart.addItem(1L, 1, price, 10);
            cart.addItem(2L, 2, price, 10);
            cart.addItem(3L, 3, price, 10);

            assertThat(cart.getItems()).hasSize(3);

            // when
            cart.clear();

            // then
            assertThat(cart.getItems()).isEmpty();
            assertThat(cart.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("총 금액 계산 시")
    class GetTotalAmount {

        @ParameterizedTest
        @MethodSource("provideTotalAmountScenarios")
        @DisplayName("장바구니 내 모든 상품의 총 금액을 계산한다")
        void getTotalAmount_CalculatesCorrectly(
            String scenario,
            Money price1,
            int quantity1,
            Money price2,
            int quantity2,
            Money expectedTotal
        ) {
            // given
            Cart cart = Cart.createNew(1L);

            cart.addItem(1L, quantity1, price1, 10);
            if (price2 != null) {
                cart.addItem(2L, quantity2, price2, 10);
            }

            // when
            Money totalAmount = cart.getTotalAmount();

            // then
            assertThat(totalAmount).isEqualTo(expectedTotal);
        }

        static Stream<Arguments> provideTotalAmountScenarios() {
            return Stream.of(
                Arguments.of(
                    "단일 상품",
                    Money.from(new BigDecimal("10000")), 2,
                    null, 0,
                    Money.from(new BigDecimal("20000"))
                ),
                Arguments.of(
                    "여러 상품",
                    Money.from(new BigDecimal("10000")), 2,
                    Money.from(new BigDecimal("5000")), 3,
                    Money.from(new BigDecimal("35000"))
                ),
                Arguments.of(
                    "수량 1인 상품",
                    Money.from(new BigDecimal("15000")), 1,
                    null, 0,
                    Money.from(new BigDecimal("15000"))
                )
            );
        }

        @Test
        @DisplayName("빈 장바구니의 총 금액은 0이다")
        void getTotalAmount_EmptyCart_ReturnsZero() {
            // given
            Cart cart = Cart.createNew(1L);

            // when
            Money totalAmount = cart.getTotalAmount();

            // then
            assertThat(totalAmount).isEqualTo(Money.ZERO);
        }
    }

    @Nested
    @DisplayName("예상 포인트 계산 시")
    class CalculateExpectedPoints {

        @ParameterizedTest
        @MethodSource("providePointCalculationScenarios")
        @DisplayName("카테고리 가중치와 쿠폰 배수를 적용하여 포인트를 계산한다")
        void calculateExpectedPoints_CalculatesCorrectly(
            String scenario,
            Money totalAmount,
            BigDecimal categoryWeight,
            BigDecimal couponMultiplier,
            BigDecimal expectedPoints
        ) {
            // given
            Cart cart = Cart.createNew(1L);
            cart.addItem(1L, 1, totalAmount, 10);

            // when
            BigDecimal points = cart.calculateExpectedPoints(categoryWeight, couponMultiplier);

            // then
            assertThat(points).isEqualByComparingTo(expectedPoints);
        }

        static Stream<Arguments> providePointCalculationScenarios() {
            return Stream.of(
                Arguments.of(
                    "기본 적립률",
                    Money.from(new BigDecimal("10000")),
                    new BigDecimal("0.01"),
                    BigDecimal.ONE,
                    new BigDecimal("100")
                ),
                Arguments.of(
                    "VIP 적립률",
                    Money.from(new BigDecimal("10000")),
                    new BigDecimal("0.02"),
                    BigDecimal.ONE,
                    new BigDecimal("200")
                ),
                Arguments.of(
                    "쿠폰 배수 적용",
                    Money.from(new BigDecimal("10000")),
                    new BigDecimal("0.01"),
                    new BigDecimal("2"),
                    new BigDecimal("200")
                ),
                Arguments.of(
                    "소수점 버림",
                    Money.from(new BigDecimal("9999")),
                    new BigDecimal("0.01"),
                    BigDecimal.ONE,
                    new BigDecimal("99")
                )
            );
        }
    }
}
