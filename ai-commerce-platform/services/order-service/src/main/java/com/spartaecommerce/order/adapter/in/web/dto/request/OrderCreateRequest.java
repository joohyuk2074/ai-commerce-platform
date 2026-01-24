package com.spartaecommerce.order.adapter.in.web.dto.request;

import com.spartaecommerce.domain.Passport;
import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderItemCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "주문 생성 요청")
public record OrderCreateRequest(
    @Schema(
        description = "주문할 상품 목록",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "[{\"productId\": 1, \"quantity\": 2}]"
    )
    @NotNull
    List<OrderItemCreateRequest> orderItemCreateRequests,

    @Schema(
        description = "배송지 주소",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "서울시 강남구 테헤란로 123"
    )
    @NotBlank
    String shippingAddress,

    @Schema(
        description = "사용할 포인트 금액 (선택)",
        example = "5000"
    )
    BigDecimal usePointAmount
) {

  public OrderCreateRequest {
    if (orderItemCreateRequests == null) {
      orderItemCreateRequests = List.of();
    }
  }

  public OrderCreateCommand toCommand(Passport passport) {
    List<OrderItemCreateCommand> orderItemCreateCommands = orderItemCreateRequests.stream()
        .map(OrderItemCreateRequest::toCommand)
        .toList();

    return new OrderCreateCommand(
        passport,
        orderItemCreateCommands,
        shippingAddress,
        usePointAmount != null ? usePointAmount : BigDecimal.ZERO
    );
  }

  @Schema(description = "주문 상품 항목")
  public record OrderItemCreateRequest(
      @Schema(
          description = "상품 ID",
          requiredMode = Schema.RequiredMode.REQUIRED,
          example = "1"
      )
      @NotNull
      @Positive
      Long productId,

      @Schema(
          description = "주문 수량",
          requiredMode = Schema.RequiredMode.REQUIRED,
          example = "2"
      )
      @Positive
      @NotNull
      Integer quantity
  ) {

    public OrderItemCreateCommand toCommand() {
      return new OrderItemCreateCommand(productId, quantity);
    }
  }
}
