package com.spartaecommerce.order.application.dto.command;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreateCommand(
    Long userId,
    List<OrderItemCreateCommand> orderItemCreateCommands,
    String shippingAddress,
    BigDecimal usePointAmount
) {

}