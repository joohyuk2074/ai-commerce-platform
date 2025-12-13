package com.spartaecommerce.cart.application.service;

import com.spartaecommerce.cart.application.CartItemAddService;
import com.spartaecommerce.cart.application.dto.command.CartAddItemCommand;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.out.CartFakeRepository;
import com.spartaecommerce.cart.domain.port.out.CartFakeStorage;
import com.spartaecommerce.common.config.properties.CartProperties;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.ProductFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CartItemAddService")
class CartItemAddServiceTest {

    private CartItemAddService sut;
    private ProductFakeRepository productRepository;
    private CartFakeRepository cartRepository;
    private CartFakeStorage cartStorage;
    private CartProperties cartProperties;

    @BeforeEach
    void setUp() {
        productRepository = new ProductFakeRepository();
        cartRepository = new CartFakeRepository();
        cartStorage = new CartFakeStorage();
        cartProperties = new CartProperties();
        cartProperties.setMaxItems(10);

        sut = new CartItemAddService(
            productRepository,
            cartRepository,
            cartRepository,
            cartProperties,
            cartStorage
        );

        setupProducts();
    }

    @AfterEach
    void tearDown() {
        productRepository.clear();
        cartRepository.clear();
        cartStorage.clear();
    }

    @Nested
    @DisplayName("상품 추가 시")
    class AddItem {

        @Test
        @DisplayName("신규 사용자의 장바구니에 상품을 추가한다")
        void addItem_ForNewUser_CreatesCartAndAddsItem() {
            // given
            Long userId = 1L;
            Long productId = 1L;
            Integer quantity = 2;

            CartAddItemCommand command = new CartAddItemCommand(userId, productId, quantity);

            // when
            sut.addItem(command);

            // then
            Cart cart = cartRepository.getByUserId(userId);
            assertThat(cart).isNotNull();
            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getProductId()).isEqualTo(productId);
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(quantity);

            // 캐시에도 저장되었는지 확인
            Cart cachedCart = cartStorage.get(userId).orElse(null);
            assertThat(cachedCart).isNotNull();
        }

        @Test
        @DisplayName("기존 장바구니에 새로운 상품을 추가한다")
        void addItem_ToExistingCart_AddsNewItem() {
            // given
            Long userId = 1L;
            Long firstProductId = 1L;
            Long secondProductId = 2L;

            sut.addItem(new CartAddItemCommand(userId, firstProductId, 1));

            // when
            sut.addItem(new CartAddItemCommand(userId, secondProductId, 2));

            // then
            Cart cart = cartRepository.getByUserId(userId);
            assertThat(cart.getItems()).hasSize(2);
            assertThat(cart.getItems())
                .extracting("productId")
                .containsExactlyInAnyOrder(firstProductId, secondProductId);
        }

        @Test
        @DisplayName("이미 담긴 상품을 추가하면 수량이 증가한다")
        void addItem_WithExistingProduct_IncreasesQuantity() {
            // given
            Long userId = 1L;
            Long productId = 1L;

            sut.addItem(new CartAddItemCommand(userId, productId, 2));

            // when
            sut.addItem(new CartAddItemCommand(userId, productId, 3));

            // then
            Cart cart = cartRepository.getByUserId(userId);
            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("최대 상품 개수를 초과하면 예외가 발생한다")
        void addItem_ExceedsMaxItems_ThrowsException() {
            // given
            Long userId = 1L;
            cartProperties.setMaxItems(2);

            sut.addItem(new CartAddItemCommand(userId, 1L, 1));
            sut.addItem(new CartAddItemCommand(userId, 2L, 1));

            // when & then
            assertThatThrownBy(() -> sut.addItem(new CartAddItemCommand(userId, 3L, 1)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }

        @Test
        @DisplayName("존재하지 않는 상품을 추가하면 예외가 발생한다")
        void addItem_WithNonExistentProduct_ThrowsException() {
            // given
            Long userId = 1L;
            Long nonExistentProductId = 999L;

            CartAddItemCommand command = new CartAddItemCommand(userId, nonExistentProductId, 1);

            // when & then
            assertThatThrownBy(() -> sut.addItem(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    private void setupProducts() {
        Product product1 = Product.createNew(
            "클린코드",
            "좋은 코드 작성법",
            Money.from(new BigDecimal("30000")),
            100,
            1L
        );
        productRepository.save(product1);

        Product product2 = Product.createNew(
            "리팩터링",
            "코드 개선 기법",
            Money.from(new BigDecimal("35000")),
            50,
            1L
        );
        productRepository.save(product2);

        Product product3 = Product.createNew(
            "TDD",
            "테스트 주도 개발",
            Money.from(new BigDecimal("25000")),
            80,
            1L
        );
        productRepository.save(product3);
    }
}
