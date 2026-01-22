package com.spartaecommerce.user.adapter.out.event;

import com.spartaecommerce.user.domain.event.UserCreatedEvent;
import com.spartaecommerce.user.domain.port.out.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUserEventPublisher implements UserEventPublisher {

    private static final String USER_EVENT_TOPIC = "ecommerce.event.user.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @Override
    public void publishUserCreated(UserCreatedEvent event) {
        try {
            String key = String.valueOf(event.getUserId());

            CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(USER_EVENT_TOPIC, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("User created event published successfully. Topic: {}, UserId: {}, Partition: {}, Offset: {}",
                        USER_EVENT_TOPIC, key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish user created event. Topic: {}, UserId: {}, Error: {}",
                        USER_EVENT_TOPIC, key, ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Unexpected error while publishing user created event. UserId: {}, Error: {}",
                event.getUserId(), e.getMessage(), e);
        }
    }
}
