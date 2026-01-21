package com.spartaecommerce.order.domain.port.in;

import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderStatusUpdateCommand;

public interface OrderCommandUseCase {

    Long create(OrderCreateCommand createCommand);

    void updateOrderStatus(OrderStatusUpdateCommand updateCommand);

    void cancel(Long orderId);
}
