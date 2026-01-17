package com.spartaecommerce.gateway.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final String AUDIT_LOG_TOPIC = "ecommerce.audit.user-activity";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendAuditLog(AuditLogEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(AUDIT_LOG_TOPIC, event.requestId(), json);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send audit log: requestId={}", event.requestId(), ex);
                } else {
                    log.debug("Audit log sent successfully: requestId={}, partition={}, offset={}",
                        event.requestId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit log event: requestId={}", event.requestId(), e);
        }
    }
}
