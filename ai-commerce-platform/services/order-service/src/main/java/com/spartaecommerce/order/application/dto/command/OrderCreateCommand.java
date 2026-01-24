package com.spartaecommerce.order.application.dto.command;


import com.spartaecommerce.domain.Passport;
import java.math.BigDecimal;
import java.util.List;

public record OrderCreateCommand(
    Passport passport,
    List<OrderItemCreateCommand> orderItemCreateCommands,
    String shippingAddress,
    BigDecimal usePointAmount
) {

}