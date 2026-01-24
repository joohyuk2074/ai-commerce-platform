package com.spartaecommerce.order.domain.event;

import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderEvent {

  private String eventType;
  private Long orderId;
  private Long userId;
  private OrderStatus orderStatus;
  private BigDecimal totalAmount;
  private String currency;
  private String paymentMethod;
  private List<OrderItemEvent> items;
  private BigDecimal pointsUsed;
  private BigDecimal pointsEarned;
  private ShippingAddressEvent shippingAddress;
  private LocalDateTime timestamp;

  public static OrderEvent from(Order order) {
    return OrderEvent.builder()
        .eventType("ORDER_CREATED")
        .orderId(order.getOrderId())
        .userId(order.getUserId())
        .orderStatus(order.getStatus())
        .totalAmount(order.calculateTotalAmount())
        .currency("KRW")
        .paymentMethod("POINT")
        .items(order.getOrderItems().stream()
            .map(OrderItemEvent::from)
            .collect(Collectors.toList()))
        .pointsUsed(order.getUsedPointAmount() != null ? order.getUsedPointAmount().amount()
            : BigDecimal.ZERO)
        .pointsEarned(order.getEarnedPointAmount() != null ? order.getEarnedPointAmount().amount()
            : BigDecimal.ZERO)
        .shippingAddress(ShippingAddressEvent.from(order.getShippingAddress()))
        .timestamp(LocalDateTime.now())
        .build();
  }

  @Getter
  @Builder
  public static class OrderItemEvent {

    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    public static OrderItemEvent from(OrderItem item) {
      return OrderItemEvent.builder()
          .productId(item.getProductId())
          .productName(item.getProductName())
          .categoryId(null) // Will be enriched later
          .categoryName(null) // Will be enriched later
          .quantity(item.getQuantity())
          .price(item.getProductPrice().amount())
          .subtotal(item.getTotalPrice().amount())
          .build();
    }

    public void enrichCategory(Long categoryId, String categoryName) {
      this.categoryId = categoryId;
      this.categoryName = categoryName;
    }
  }

  @Getter
  @Builder
  public static class ShippingAddressEvent {

    private String country;
    private String city;
    private String zipCode;

    public static ShippingAddressEvent from(String shippingAddress) {
      // TODO: Parse shipping address properly
      // For now, just use simple parsing
      return ShippingAddressEvent.builder()
          .country("KR")
          .city("Seoul")
          .zipCode("")
          .build();
    }
  }
}
