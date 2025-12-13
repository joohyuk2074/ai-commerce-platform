package com.spartaecommerce.product.application.service;

import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.out.CategoryFakeRepository;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.domain.repository.OrderFakeRepository;
import com.spartaecommerce.product.application.dto.command.ProductRegisterCommand;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.ProductFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductCommandService")
class ProductCommandServiceTest {

    private ProductCommandService sut;
    private ProductFakeRepository productRepository;
    private CategoryFakeRepository categoryRepository;
    private OrderFakeRepository orderRepository;

    @BeforeEach
    void setUp() {
        productRepository = new ProductFakeRepository();
        categoryRepository = new CategoryFakeRepository();
        orderRepository = new OrderFakeRepository();

        sut = new ProductCommandService(
            productRepository,
            productRepository,
            categoryRepository,
            orderRepository
        );

        // 테스트용 카테고리 설정
        setupCategories();
    }

    @AfterEach
    void tearDown() {
        productRepository.clear();
        categoryRepository.clear();
        orderRepository.clear();
    }

    @Nested
    @DisplayName("상품 등록 시")
    class Register {

        @Test
        @DisplayName("유효한 정보로 상품을 등록한다")
        void register_WithValidData_RegistersProduct() {
            // given
            ProductRegisterCommand command = new ProductRegisterCommand(
                "클린코드",
                "좋은 코드 작성법",
                Money.from(new BigDecimal("30000")),
                100,
                1L
            );

            // when
            Long productId = sut.register(command);

            // then
            assertThat(productId).isNotNull();

            Product savedProduct = productRepository.getById(productId);
            assertThat(savedProduct.getName()).isEqualTo("클린코드");
            assertThat(savedProduct.getDescription()).isEqualTo("좋은 코드 작성법");
            assertThat(savedProduct.getPrice()).isEqualTo(Money.from(new BigDecimal("30000")));
            assertThat(savedProduct.getStock()).isEqualTo(100);
            assertThat(savedProduct.getCategoryId()).isEqualTo(1L);
            assertThat(savedProduct.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 등록하면 예외가 발생한다")
        void register_WithNonExistentCategory_ThrowsException() {
            // given
            Long nonExistentCategoryId = 999L;
            ProductRegisterCommand command = new ProductRegisterCommand(
                "클린코드",
                "좋은 코드 작성법",
                Money.from(new BigDecimal("30000")),
                100,
                nonExistentCategoryId
            );

            // when & then
            assertThatThrownBy(() -> sut.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("이미 존재하는 상품명으로 등록하면 예외가 발생한다")
        void register_WithDuplicateName_ThrowsException() {
            // given
            String duplicateName = "클린코드";

            // 첫 번째 상품 등록
            productRepository.save(createProduct(duplicateName));

            // 같은 이름으로 두 번째 상품 등록 시도
            ProductRegisterCommand command = new ProductRegisterCommand(
                duplicateName,
                "다른 설명",
                Money.from(new BigDecimal("35000")),
                50,
                1L
            );

            // when & then
            assertThatThrownBy(() -> sut.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("상품 수정 시")
    class Update {

        @Test
        @DisplayName("유효한 정보로 상품을 수정한다")
        void update_WithValidData_UpdatesProduct() {
            // given
            Long productId = productRepository.save(createProduct("클린코드"));

            ProductUpdateCommand command = new ProductUpdateCommand(
                productId,
                "리팩터링",
                "개선된 설명",
                Money.from(new BigDecimal("35000")),
                150,
                2L
            );

            // when
            sut.update(command);

            // then
            Product updatedProduct = productRepository.getById(productId);
            assertThat(updatedProduct.getName()).isEqualTo("리팩터링");
            assertThat(updatedProduct.getDescription()).isEqualTo("개선된 설명");
            assertThat(updatedProduct.getPrice()).isEqualTo(Money.from(new BigDecimal("35000")));
            assertThat(updatedProduct.getStock()).isEqualTo(150);
            assertThat(updatedProduct.getCategoryId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 수정하면 예외가 발생한다")
        void update_WithNonExistentCategory_ThrowsException() {
            // given
            Long productId = productRepository.save(createProduct("클린코드"));
            Long nonExistentCategoryId = 999L;

            ProductUpdateCommand command = new ProductUpdateCommand(
                productId,
                "리팩터링",
                "개선된 설명",
                Money.from(new BigDecimal("35000")),
                150,
                nonExistentCategoryId
            );

            // when & then
            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("다른 상품의 이름으로 수정하면 예외가 발생한다")
        void update_WithOtherProductName_ThrowsException() {
            // given
            String existingName = "클린코드";
            productRepository.save(createProduct(existingName)); // 첫 번째 상품

            Long secondProductId = productRepository.save(createProduct("리팩터링")); // 두 번째 상품

            // 두 번째 상품의 이름을 첫 번째 상품의 이름으로 변경 시도
            ProductUpdateCommand command = new ProductUpdateCommand(
                secondProductId,
                existingName,
                "설명",
                Money.from(new BigDecimal("35000")),
                150,
                1L
            );

            // when & then
            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("자신의 이름으로 수정하면 정상적으로 수정된다")
        void update_WithSameName_UpdatesSuccessfully() {
            // given
            String productName = "클린코드";
            Long productId = productRepository.save(createProduct(productName));

            ProductUpdateCommand command = new ProductUpdateCommand(
                productId,
                productName, // 같은 이름
                "새로운 설명",
                Money.from(new BigDecimal("35000")),
                150,
                1L
            );

            // when
            sut.update(command);

            // then
            Product updatedProduct = productRepository.getById(productId);
            assertThat(updatedProduct.getName()).isEqualTo(productName);
            assertThat(updatedProduct.getDescription()).isEqualTo("새로운 설명");
        }

        @Test
        @DisplayName("존재하지 않는 상품을 수정하면 예외가 발생한다")
        void update_WithNonExistentProduct_ThrowsException() {
            // given
            Long nonExistentProductId = 999L;
            ProductUpdateCommand command = new ProductUpdateCommand(
                nonExistentProductId,
                "새 이름",
                "새 설명",
                Money.from(new BigDecimal("35000")),
                150,
                1L
            );

            // when & then
            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상품 삭제 시")
    class Delete {

        @Test
        @DisplayName("완료된 주문이 없는 상품은 정상적으로 삭제된다")
        void delete_WithNoCompletedOrders_DeletesSuccessfully() {
            // given
            Long productId = productRepository.save(createProduct("클린코드"));

            // when
            sut.delete(productId);

            // then
            Product deletedProduct = productRepository.findById(productId).orElse(null);
            assertThat(deletedProduct).isNull(); // soft delete로 조회되지 않음
        }

        @Test
        @DisplayName("완료된 주문에 포함된 상품은 삭제할 수 없다")
        void delete_WithCompletedOrders_ThrowsException() {
            // given
            Long productId = productRepository.save(createProduct("클린코드"));

            // 완료된 주문 생성
            Order completedOrder = createOrder(1L, productId, OrderStatus.COMPLETED);
            orderRepository.save(completedOrder);

            // when & then
            assertThatThrownBy(() -> sut.delete(productId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }

        @Test
        @DisplayName("대기 중인 주문에 포함된 상품은 삭제할 수 있다")
        void delete_WithPendingOrders_DeletesSuccessfully() {
            // given
            Long productId = productRepository.save(createProduct("클린코드"));

            // 대기 중인 주문 생성
            Order pendingOrder = createOrder(1L, productId, OrderStatus.PENDING);
            orderRepository.save(pendingOrder);

            // when
            sut.delete(productId);

            // then
            Product deletedProduct = productRepository.findById(productId).orElse(null);
            assertThat(deletedProduct).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 상품을 삭제하면 예외가 발생한다")
        void delete_WithNonExistentProduct_ThrowsException() {
            // given
            Long nonExistentProductId = 999L;

            // when & then
            assertThatThrownBy(() -> sut.delete(nonExistentProductId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    private void setupCategories() {
        Category category1 = Category.createNew("도서", "도서 카테고리", null);
        categoryRepository.save(category1);

        Category category2 = Category.createNew("전자제품", "전자제품 카테고리", null);
        categoryRepository.save(category2);
    }

    private Product createProduct(String name) {
        return Product.builder()
            .name(name)
            .description("설명")
            .price(Money.from(new BigDecimal("30000")))
            .stock(100)
            .categoryId(1L)
            .deleted(false)
            .build();
    }

    private Order createOrder(Long userId, Long productId, OrderStatus status) {
        OrderItem orderItem = OrderItem.builder()
            .productId(productId)
            .quantity(5)
            .build();

        return Order.builder()
            .userId(userId)
            .orderItems(List.of(orderItem))
            .status(status)
            .shippingAddress("서울시 강남구")
            .build();
    }
}
