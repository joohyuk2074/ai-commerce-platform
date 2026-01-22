package com.spartaecommerce.order.domain.port.out;

import com.spartaecommerce.order.domain.event.OrderEvent;

public interface OrderEventPublisher {

    void publishOrderCreated(OrderEvent event);
}
