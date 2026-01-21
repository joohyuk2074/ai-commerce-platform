package com.spartaecommerce.order.adapter.out.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.spartaecommerce.order.domain.event.OrderEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Kafka message payload (matches Elasticsearch event-order-template.json)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDto {

    @JsonProperty("@timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime timestamp;

    private String eventType;

    private String orderId;

    private String userId;

    private String orderStatus;

    private BigDecimal totalAmount;

    private String currency;

    private String paymentMethod;

    private List<OrderItemDto> items;

    private Integer pointsUsed;

    private Integer pointsEarned;

    private ShippingAddressDto shippingAddress;

    private String environment;

    private String platform;

    private List<String> tags;

    public static OrderEventDto from(OrderEvent event) {
        return OrderEventDto.builder()
            .timestamp(event.getTimestamp())
            .eventType(event.getEventType())
            .orderId(String.valueOf(event.getOrderId()))
            .userId(String.valueOf(event.getUserId()))
            .orderStatus(event.getOrderStatus().name())
            .totalAmount(event.getTotalAmount())
            .currency(event.getCurrency())
            .paymentMethod(event.getPaymentMethod())
            .items(event.getItems().stream()
                .map(OrderItemDto::from)
                .collect(Collectors.toList()))
            .pointsUsed(event.getPointsUsed().intValue())
            .pointsEarned(event.getPointsEarned().intValue())
            .shippingAddress(ShippingAddressDto.from(event.getShippingAddress()))
            .environment("dev")
            .platform("sparta-ecommerce")
            .tags(List.of("order", "ecommerce"))
            .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String productId;
        private String productName;
        private String categoryId;
        private String categoryName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;

        public static OrderItemDto from(OrderEvent.OrderItemEvent item) {
            return OrderItemDto.builder()
                .productId(String.valueOf(item.getProductId()))
                .productName(item.getProductName())
                .categoryId(item.getCategoryId() != null ? String.valueOf(item.getCategoryId()) : null)
                .categoryName(item.getCategoryName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {
        private String country;
        private String city;
        private String zipCode;

        public static ShippingAddressDto from(OrderEvent.ShippingAddressEvent address) {
            return ShippingAddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .zipCode(address.getZipCode())
                .build();
        }
    }
}
