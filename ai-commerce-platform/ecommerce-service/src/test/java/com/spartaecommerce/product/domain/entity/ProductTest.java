package com.spartaecommerce.product.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Product 도메인 엔티티")
class ProductTest {

    @Nested
    @DisplayName("상품 생성 시")
    class CreateNew {

        @Test
        @DisplayName("유효한 정보로 상품을 생성한다")
        void createNew_WithValidData_CreatesProduct() {
            // given
            String name = "클린코드";
            String description = "좋은 코드 작성법";
            Money price = Money.from(new BigDecimal("30000"));
            Integer stock = 100;
            Long categoryId = 1L;

            // when
            Product product = Product.createNew(name, description, price, stock, categoryId);

            // then
            assertThat(product.getName()).isEqualTo(name);
            assertThat(product.getDescription()).isEqualTo(description);
            assertThat(product.getPrice()).isEqualTo(price);
            assertThat(product.getStock()).isEqualTo(stock);
            assertThat(product.getCategoryId()).isEqualTo(categoryId);
            assertThat(product.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("상품 업데이트 시")
    class Update {

        @ParameterizedTest
        @MethodSource("provideValidUpdateScenarios")
        @DisplayName("유효한 값으로 상품 정보를 업데이트한다")
        void update_WithValidData_UpdatesProduct(
            String scenario,
            String newName,
            String newDescription,
            BigDecimal newPrice,
            Integer newStock,
            Long newCategoryId
        ) {
            // given
            Product product = createProduct();

            // when
            product.update(
                newName,
                newDescription,
                newPrice != null ? Money.from(newPrice) : null,
                newStock,
                newCategoryId
            );

            // then
            if (newName != null && !newName.isBlank()) {
                assertThat(product.getName()).isEqualTo(newName);
            }
            if (newDescription != null && !newDescription.isBlank()) {
                assertThat(product.getDescription()).isEqualTo(newDescription);
            }
            if (newPrice != null) {
                assertThat(product.getPrice().amount()).isEqualByComparingTo(newPrice);
            }
            if (newStock != null) {
                assertThat(product.getStock()).isEqualTo(newStock);
            }
            if (newCategoryId != null) {
                assertThat(product.getCategoryId()).isEqualTo(newCategoryId);
            }
        }

        static Stream<Arguments> provideValidUpdateScenarios() {
            return Stream.of(
                Arguments.of("모든 필드 업데이트", "리팩터링", "개선된 설명", new BigDecimal("35000"), 150, 2L),
                Arguments.of("이름만 업데이트", "새 이름", null, null, null, null),
                Arguments.of("가격만 업데이트", null, null, new BigDecimal("40000"), null, null),
                Arguments.of("재고만 업데이트", null, null, null, 200, null),
                Arguments.of("카테고리만 업데이트", null, null, null, null, 3L)
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("빈 이름으로 업데이트하면 기존 값이 유지된다")
        void update_WithBlankName_KeepsOriginalName(String blankName) {
            // given
            Product product = createProduct();
            String originalName = product.getName();

            // when
            product.update(blankName, null, null, null, null);

            // then
            assertThat(product.getName()).isEqualTo(originalName);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("빈 설명으로 업데이트하면 기존 값이 유지된다")
        void update_WithBlankDescription_KeepsOriginalDescription(String blankDescription) {
            // given
            Product product = createProduct();
            String originalDescription = product.getDescription();

            // when
            product.update(null, blankDescription, null, null, null);

            // then
            assertThat(product.getDescription()).isEqualTo(originalDescription);
        }

        @Test
        @DisplayName("삭제된 상품은 업데이트할 수 없다")
        void update_DeletedProduct_ThrowsException() {
            // given
            Product product = createProduct();
            product.delete();

            // when & then
            assertThatThrownBy(() -> product.update("새 이름", null, null, null, null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }
    }

    @Nested
    @DisplayName("재고 차감 시")
    class DeductQuantity {

        @ParameterizedTest
        @MethodSource("provideValidDeductScenarios")
        @DisplayName("충분한 재고가 있으면 정상적으로 차감된다")
        void deductQuantity_WithSufficientStock_DeductsSuccessfully(
            String scenario,
            int initialStock,
            int deductQuantity,
            int expectedStock
        ) {
            // given
            Product product = createProductWithStock(initialStock);

            // when
            product.deductQuantity(deductQuantity);

            // then
            assertThat(product.getStock()).isEqualTo(expectedStock);
        }

        static Stream<Arguments> provideValidDeductScenarios() {
            return Stream.of(
                Arguments.of("일부 차감", 100, 30, 70),
                Arguments.of("전량 차감", 50, 50, 0),
                Arguments.of("1개 차감", 10, 1, 9),
                Arguments.of("대량 차감", 1000, 999, 1)
            );
        }

        @ParameterizedTest
        @MethodSource("provideInsufficientStockScenarios")
        @DisplayName("재고가 부족하면 예외가 발생한다")
        void deductQuantity_WithInsufficientStock_ThrowsException(
            String scenario,
            int initialStock,
            int deductQuantity
        ) {
            // given
            Product product = createProductWithStock(initialStock);

            // when & then
            assertThatThrownBy(() -> product.deductQuantity(deductQuantity))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessException = (BusinessException) ex;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                    assertThat(businessException.getMessage())
                        .contains("Available: " + initialStock, "Requested: " + deductQuantity);
                });
        }

        static Stream<Arguments> provideInsufficientStockScenarios() {
            return Stream.of(
                Arguments.of("재고 초과 요청", 10, 11),
                Arguments.of("재고 0인데 요청", 0, 1),
                Arguments.of("재고보다 훨씬 많이 요청", 5, 100)
            );
        }
    }

    @Nested
    @DisplayName("재고 복구 시")
    class RestoreQuantity {

        @ParameterizedTest
        @MethodSource("provideValidRestoreScenarios")
        @DisplayName("유효한 수량으로 재고를 복구한다")
        void restoreQuantity_WithValidQuantity_RestoresSuccessfully(
            String scenario,
            int initialStock,
            int restoreQuantity,
            int expectedStock
        ) {
            // given
            Product product = createProductWithStock(initialStock);

            // when
            product.restoreQuantity(restoreQuantity);

            // then
            assertThat(product.getStock()).isEqualTo(expectedStock);
        }

        static Stream<Arguments> provideValidRestoreScenarios() {
            return Stream.of(
                Arguments.of("일부 복구", 50, 30, 80),
                Arguments.of("1개 복구", 10, 1, 11),
                Arguments.of("대량 복구", 100, 900, 1000),
                Arguments.of("재고 0에서 복구", 0, 50, 50)
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidRestoreScenarios")
        @DisplayName("유효하지 않은 수량은 예외가 발생한다")
        void restoreQuantity_WithInvalidQuantity_ThrowsException(
            String scenario,
            Integer restoreQuantity
        ) {
            // given
            Product product = createProductWithStock(50);

            // when & then
            assertThatThrownBy(() -> product.restoreQuantity(restoreQuantity))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }

        static Stream<Arguments> provideInvalidRestoreScenarios() {
            return Stream.of(
                Arguments.of("null 수량", null),
                Arguments.of("0 수량", 0),
                Arguments.of("음수 수량", -10)
            );
        }
    }

    @Nested
    @DisplayName("상품 삭제 시")
    class Delete {

        @Test
        @DisplayName("상품을 삭제 상태로 변경한다")
        void delete_SetsDeletedFlag() {
            // given
            Product product = createProduct();
            assertThat(product.isDeleted()).isFalse();

            // when
            product.delete();

            // then
            assertThat(product.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("삭제된 상품은 다시 삭제할 수 있다")
        void delete_AlreadyDeletedProduct_DoesNotThrowException() {
            // given
            Product product = createProduct();
            product.delete();

            // when & then
            product.delete(); // 예외가 발생하지 않음
            assertThat(product.isDeleted()).isTrue();
        }
    }

    private Product createProduct() {
        return Product.builder()
            .productId(1L)
            .name("클린코드")
            .description("좋은 코드 작성법")
            .price(Money.from(new BigDecimal("30000")))
            .stock(100)
            .categoryId(1L)
            .deleted(false)
            .build();
    }

    private Product createProductWithStock(int stock) {
        return Product.builder()
            .productId(1L)
            .name("클린코드")
            .description("좋은 코드 작성법")
            .price(Money.from(new BigDecimal("30000")))
            .stock(stock)
            .categoryId(1L)
            .deleted(false)
            .build();
    }
}
