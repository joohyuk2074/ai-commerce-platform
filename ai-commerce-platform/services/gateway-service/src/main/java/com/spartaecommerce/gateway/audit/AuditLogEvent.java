package com.spartaecommerce.gateway.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditLogEvent(
    String requestId,
    String timestamp,
    String userId,
    String username,
    String method,
    String path,
    String query,
    Integer statusCode,
    Long responseTime,
    String clientIp,
    String userAgent,
    String action,
    String service,
    String resource,
    Map<String, Object> metadata
) {
}
