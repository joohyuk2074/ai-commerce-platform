package com.spartaecommerce.refund.application.service;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.domain.port.out.OrderFakeRepository;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.ProductFakeRepository;
import com.spartaecommerce.refund.application.dto.command.RefundCreateCommand;
import com.spartaecommerce.refund.application.dto.command.RefundProcessCommand;
import com.spartaecommerce.refund.domain.entity.Refund;
import com.spartaecommerce.refund.domain.entity.RefundStatus;
import com.spartaecommerce.refund.domain.port.out.RefundFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RefundCommandService")
class RefundCommandServiceTest {

    private RefundCommandService sut;
    private RefundFakeRepository refundRepository;
    private OrderFakeRepository orderRepository;
    private ProductFakeRepository productRepository;

    @BeforeEach
    void setUp() {
        refundRepository = new RefundFakeRepository();
        orderRepository = new OrderFakeRepository();
        productRepository = new ProductFakeRepository();

        sut = new RefundCommandService(
            refundRepository,
            refundRepository,
            orderRepository,
            productRepository,
            productRepository
        );
    }

    @AfterEach
    void tearDown() {
        refundRepository.clear();
        orderRepository.clear();
        productRepository.clear();
    }

    @Nested
    @DisplayName("환불 생성 시")
    class Create {

        @Test
        @DisplayName("완료된 주문에 대해 환불을 생성한다")
        void create_WithCompletedOrder_CreatesRefund() {
            // given
            Long orderId = createCompletedOrder(1L);

            RefundCreateCommand command = new RefundCreateCommand(
                1L,
                orderId,
                "상품 불량"
            );

            // when
            Long refundId = sut.create(command);

            // then
            assertThat(refundId).isNotNull();

            Refund savedRefund = refundRepository.getById(refundId);
            assertThat(savedRefund.getUserId()).isEqualTo(1L);
            assertThat(savedRefund.getOrderId()).isEqualTo(orderId);
            assertThat(savedRefund.getReason()).isEqualTo("상품 불량");
            assertThat(savedRefund.getStatus()).isEqualTo(RefundStatus.PENDING);
        }

        @Test
        @DisplayName("완료되지 않은 주문은 환불을 생성할 수 없다")
        void create_WithNonCompletedOrder_ThrowsException() {
            // given
            Long orderId = createOrderWithStatus(1L, OrderStatus.PENDING);

            RefundCreateCommand command = new RefundCreateCommand(
                1L,
                orderId,
                "상품 불량"
            );

            // when & then
            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessException = (BusinessException) ex;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                    assertThat(businessException.getMessage())
                        .contains("Only completed orders can be refunded");
                });
        }

        @Test
        @DisplayName("이미 환불이 존재하는 주문은 다시 환불을 생성할 수 없다")
        void create_WithExistingRefund_ThrowsException() {
            // given
            Long orderId = createCompletedOrder(1L);

            // 첫 번째 환불 생성
            RefundCreateCommand firstCommand = new RefundCreateCommand(1L, orderId, "상품 불량");
            sut.create(firstCommand);

            // 같은 주문에 대해 두 번째 환불 생성 시도
            RefundCreateCommand secondCommand = new RefundCreateCommand(1L, orderId, "배송 지연");

            // when & then
            assertThatThrownBy(() -> sut.create(secondCommand))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("존재하지 않는 주문은 환불을 생성할 수 없다")
        void create_WithNonExistentOrder_ThrowsException() {
            // given
            Long nonExistentOrderId = 999L;
            RefundCreateCommand command = new RefundCreateCommand(
                1L,
                nonExistentOrderId,
                "상품 불량"
            );

            // when & then
            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("환불 처리 시")
    class Process {

        @Test
        @DisplayName("환불을 승인하면 상품 재고가 복구된다")
        void process_ApproveRefund_RestoresProductStock() {
            // given
            Long productId = createProduct("테스트 상품", 100);
            Long orderId = createCompletedOrderWithProduct(1L, productId, 10);
            Long refundId = createRefund(1L, orderId);

            // 재고 차감 (주문 시뮬레이션)
            Product product = productRepository.getById(productId);
            product.deductQuantity(10);
            productRepository.save(product);

            assertThat(productRepository.getById(productId).getStock()).isEqualTo(90);

            RefundProcessCommand command = new RefundProcessCommand(
                refundId,
                RefundStatus.APPROVED
            );

            // when
            sut.process(command);

            // then
            Refund processedRefund = refundRepository.getById(refundId);
            assertThat(processedRefund.getStatus()).isEqualTo(RefundStatus.APPROVED);

            // 재고 복구 확인
            Product restoredProduct = productRepository.getById(productId);
            assertThat(restoredProduct.getStock()).isEqualTo(100);
        }

        @Test
        @DisplayName("환불을 거부하면 상품 재고는 변경되지 않는다")
        void process_RejectRefund_DoesNotRestoreStock() {
            // given
            Long productId = createProduct("테스트 상품", 100);
            Long orderId = createCompletedOrderWithProduct(1L, productId, 10);
            Long refundId = createRefund(1L, orderId);

            // 재고 차감
            Product product = productRepository.getById(productId);
            product.deductQuantity(10);
            productRepository.save(product);

            assertThat(productRepository.getById(productId).getStock()).isEqualTo(90);

            RefundProcessCommand command = new RefundProcessCommand(
                refundId,
                RefundStatus.REJECTED
            );

            // when
            sut.process(command);

            // then
            Refund processedRefund = refundRepository.getById(refundId);
            assertThat(processedRefund.getStatus()).isEqualTo(RefundStatus.REJECTED);

            // 재고 변경 없음 확인
            Product unchangedProduct = productRepository.getById(productId);
            assertThat(unchangedProduct.getStock()).isEqualTo(90);
        }

        @Test
        @DisplayName("존재하지 않는 환불은 처리할 수 없다")
        void process_WithNonExistentRefund_ThrowsException() {
            // given
            Long nonExistentRefundId = 999L;
            RefundProcessCommand command = new RefundProcessCommand(
                nonExistentRefundId,
                RefundStatus.APPROVED
            );

            // when & then
            assertThatThrownBy(() -> sut.process(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("PENDING 상태가 아닌 환불 상태는 유효하지 않다")
        void process_WithPendingStatus_ThrowsException() {
            // given
            Long orderId = createCompletedOrder(1L);
            Long refundId = createRefund(1L, orderId);

            RefundProcessCommand command = new RefundProcessCommand(
                refundId,
                RefundStatus.PENDING // PENDING으로 변경 시도
            );

            // when & then
            assertThatThrownBy(() -> sut.process(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessException = (BusinessException) ex;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                    assertThat(businessException.getMessage())
                        .contains("Invalid refund status");
                });
        }
    }

    private Long createProduct(String name, int stock) {
        Product product = Product.builder()
            .name(name)
            .description("설명")
            .price(Money.from(new BigDecimal("10000")))
            .stock(stock)
            .categoryId(1L)
            .deleted(false)
            .build();
        return productRepository.save(product);
    }

    private Long createCompletedOrder(Long userId) {
        return createOrderWithStatus(userId, OrderStatus.COMPLETED);
    }

    private Long createOrderWithStatus(Long userId, OrderStatus status) {
        Long productId = createProduct("테스트 상품", 100);
        return createOrderWithProductAndStatus(userId, productId, 5, status);
    }

    private Long createCompletedOrderWithProduct(Long userId, Long productId, int quantity) {
        return createOrderWithProductAndStatus(userId, productId, quantity, OrderStatus.COMPLETED);
    }

    private Long createOrderWithProductAndStatus(Long userId, Long productId, int quantity, OrderStatus status) {
        OrderItem orderItem = OrderItem.builder()
            .productId(productId)
            .quantity(quantity)
            .build();

        Order order = Order.builder()
            .userId(userId)
            .orderItems(List.of(orderItem))
            .status(status)
            .shippingAddress("서울시 강남구")
            .build();

        return orderRepository.save(order);
    }

    private Long createRefund(Long userId, Long orderId) {
        Refund refund = Refund.createNew(userId, orderId, "테스트 사유");
        return refundRepository.save(refund);
    }
}
