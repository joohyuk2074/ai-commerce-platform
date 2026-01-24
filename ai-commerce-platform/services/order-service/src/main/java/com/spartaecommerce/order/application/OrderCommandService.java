package com.spartaecommerce.order.application;

import com.spartaecommerce.common.infrastructure.lock.DistributedLock;
import com.spartaecommerce.domain.Passport;
import com.spartaecommerce.domain.vo.Money;
import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderItemCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderStatusUpdateCommand;
import com.spartaecommerce.order.domain.entity.Category;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.domain.entity.Product;
import com.spartaecommerce.order.domain.event.OrderEvent;
import com.spartaecommerce.order.domain.port.in.OrderCommandUseCase;
import com.spartaecommerce.order.domain.port.out.LoadOrderPort;
import com.spartaecommerce.order.domain.port.out.LoadProductPort;
import com.spartaecommerce.order.domain.port.out.OrderEventPublisher;
import com.spartaecommerce.order.domain.port.out.SaveOrderPort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService implements OrderCommandUseCase {

  private final SaveOrderPort saveOrderPort;
  private final LoadOrderPort loadOrderPort;
  private final LoadProductPort loadProductPort;

  private final OrderItemProcessor orderItemProcessor;
  private final OrderPointProcessor orderPointProcessor;

  private final OrderHistoryRecorder orderHistoryRecorder;

  private final OrderEventPublisher orderEventPublisher;

  // TODO: Outbox + Saga 패턴 적용하여 락 범위 최소화
  @Override
  @DistributedLock(
      key = "'product:stock:' + #createCommand.orderItemCreateCommands().get(0).productId()",
      errorMessage = "상품 재고 처리 중입니다. 잠시 후 다시 시도해주세요."
  )
  public Long create(OrderCreateCommand createCommand) {
    Passport passport = createCommand.passport();

    List<OrderItemCreateCommand> orderItemCreateCommands = createCommand.orderItemCreateCommands();
    List<Product> products = loadProducts(orderItemCreateCommands);

    Map<Long, Product> productIdToProduct = orderItemProcessor.indexProductsByProductId(products);
    Map<Long, Category> productIdToCategory = orderItemProcessor.indexCategoriesByProductId(
        products);

    // 재고 검증 + 차감
    orderItemProcessor.deductStocks(products, orderItemCreateCommands);

    // 주문 생성 (먼저 저장하여 orderId 획득)
    Order order = createOrderAggregate(createCommand, productIdToProduct);
    Long createdOrderId = saveOrderPort.save(order);

    // 포인트 계산 (Passport 기반)
    BigDecimal earnedPoints = orderPointProcessor.calculateExpectedPoints(
        order.getOrderItems(),
        passport,
        productIdToCategory
    );

    // 포인트 사용 처리 (orderId 포함)
    Money usedPoints = orderPointProcessor.usePoints(
        passport.userId(),
        createCommand.usePointAmount(),
        createdOrderId
    );

    // 포인트 적립 처리 (orderId 포함)
    Money earnedPointsMoney = orderPointProcessor.earnPoints(
        passport.userId(),
        earnedPoints,
        createdOrderId
    );

    // Order에 포인트 금액 설정 후 업데이트 (취소 시 복구/회수를 위해)
    order.setPointAmounts(usedPoints, earnedPointsMoney);
    saveOrderPort.save(order);

    // 포인트 트랜잭션 기록 (이미 ecommerce-service에서 처리됨)
    orderPointProcessor.recordEarnTransaction(passport.userId(), earnedPointsMoney, createdOrderId);
    if (!usedPoints.isZero()) {
      orderPointProcessor.recordUseTransaction(passport.userId(), usedPoints, createdOrderId);
    }

    // 주문 히스토리 기록
    orderHistoryRecorder.recordCreation(createdOrderId);

    // 주문 생성 이벤트 발행 (비동기)
    publishOrderCreatedEvent(order, productIdToCategory);

    return createdOrderId;
  }

  private void publishOrderCreatedEvent(
      Order order,
      Map<Long, Category> productIdToCategory
  ) {
    // OrderEvent 생성
    OrderEvent event = OrderEvent.from(order);

    // 카테고리 정보 enrichment
    for (OrderEvent.OrderItemEvent itemEvent : event.getItems()) {
      Category category = productIdToCategory.get(itemEvent.getProductId());
      if (category != null) {
        itemEvent.enrichCategory(category.getCategoryId(), category.getName());
      }
    }

    // 비동기로 이벤트 발행
    orderEventPublisher.publishOrderCreated(event);
  }

  @Override
  @DistributedLock(
      key = "'order:' + #updateCommand.orderId() + ':status'",
      waitTime = 3000L,
      leaseTime = 2000L,
      errorMessage = "주문 처리 중입니다. 잠시 후 다시 시도해주세요."
  )
  public void updateOrderStatus(OrderStatusUpdateCommand updateCommand) {
    if (updateCommand.orderStatus() == OrderStatus.CANCELED) {
      cancel(updateCommand.orderId());
      return;
    }

    Order order = loadOrderPort.getById(updateCommand.orderId());
    OrderStatus previousStatus = order.getStatus();

    order.updateOrderStatus(updateCommand.orderStatus());
    saveOrderPort.save(order);

    orderHistoryRecorder.recordStatusChange(
        updateCommand.orderId(),
        previousStatus,
        updateCommand.orderStatus(),
        "주문 상태 변경"
    );
  }

  @Override
  @DistributedLock(
      key = "'order:' + #orderId + ':cancel'",
      waitTime = 3000L,
      leaseTime = 5000L
  )
  public void cancel(Long orderId) {
    // 1. Order 조회
    Order order = loadOrderPort.getById(orderId);
    OrderStatus previousStatus = order.getStatus();

    // 2. 주문 취소 (도메인 로직)
    order.cancel();

    // 3. 재고 복구
    List<Long> productIds = order.getOrderItems().stream()
        .map(OrderItem::getProductId)
        .toList();

    List<Product> products = orderItemProcessor.loadProducts(
        productIds.stream()
            .map(id -> new OrderItemCreateCommand(id, 0)) // quantity는 사용되지 않음
            .toList()
    );

    Map<Long, Integer> productIdToQuantity = order.getOrderItems().stream()
        .collect(Collectors.toMap(
            OrderItem::getProductId,
            OrderItem::getQuantity
        ));
    orderItemProcessor.restoreStocks(products, productIdToQuantity);

    // 4. 사용 포인트 복구 (주문 시 사용한 포인트를 다시 적립)
    orderPointProcessor.refundUsedPoints(
        order.getUserId(),
        order.getUsedPointAmount(),
        orderId
    );

    // 5. 적립 포인트 회수 (주문으로 적립된 포인트를 차감)
    orderPointProcessor.reclaimEarnedPoints(
        order.getUserId(),
        order.getEarnedPointAmount(),
        orderId
    );

    // 6. Order 저장 (재고는 이미 ecommerce-service에서 복구됨)
    saveOrderPort.save(order);

    // 7. 취소 히스토리 기록
    orderHistoryRecorder.recordCancellation(orderId, previousStatus);
  }

  private Order createOrderAggregate(
      OrderCreateCommand createCommand,
      Map<Long, Product> productIdToProduct
  ) {
    Order order = Order.createNew(createCommand.passport().userId(),
        createCommand.shippingAddress());

    for (OrderItemCreateCommand itemCommand : createCommand.orderItemCreateCommands()) {
      Product product = productIdToProduct.get(itemCommand.productId());
      order.addOrderItem(
          product.getProductId(),
          product.getName(),
          product.getPrice(),
          itemCommand.quantity()
      );
    }

    return order;
  }

  public List<Product> loadProducts(List<OrderItemCreateCommand> orderItemCreateCommands) {
    List<Long> productIds = orderItemCreateCommands.stream()
        .map(OrderItemCreateCommand::productId)
        .toList();

    return loadProductPort.getProducts(productIds);
  }
}