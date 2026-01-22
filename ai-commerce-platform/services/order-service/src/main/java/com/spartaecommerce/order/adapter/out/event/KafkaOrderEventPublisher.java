package com.spartaecommerce.order.adapter.out.event;

import com.spartaecommerce.order.adapter.out.event.dto.OrderEventDto;
import com.spartaecommerce.order.domain.event.OrderEvent;
import com.spartaecommerce.order.domain.port.out.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter for publishing order events
 * Hexagonal Architecture: Output Adapter (implements port interface)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private static final String ORDER_EVENT_TOPIC = "ecommerce.event.order.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @Override
    public void publishOrderCreated(OrderEvent event) {
        try {
            OrderEventDto eventDto = OrderEventDto.from(event);
            String key = String.valueOf(event.getOrderId());

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(ORDER_EVENT_TOPIC, key, eventDto);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Order event published successfully. Topic: {}, Key: {}, Partition: {}, Offset: {}",
                        ORDER_EVENT_TOPIC, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish order event. Topic: {}, Key: {}, Error: {}",
                        ORDER_EVENT_TOPIC, key, ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Unexpected error while publishing order event. OrderId: {}, Error: {}",
                event.getOrderId(), e.getMessage(), e);
        }
    }
}
