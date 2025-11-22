package com.spartaecommerce.common.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditLog {

    private Long auditLogId;

    private AuditActionType action;

    private Long userId;

    private String methodName;

    private String parameters;

    private String result;

    private Long executionTimeMs;

    private Boolean success;

    private String errorMessage;

    private String ipAddress;

    private String description;

    private LocalDateTime createdAt;

    public static AuditLog createNew(
        AuditActionType action,
        Long userId,
        String methodName,
        String parameters,
        String result,
        Long executionTimeMs,
        Boolean success,
        String errorMessage,
        String ipAddress,
        String description
    ) {
        return AuditLog.builder()
            .action(action)
            .userId(userId)
            .methodName(methodName)
            .parameters(parameters)
            .result(result)
            .executionTimeMs(executionTimeMs)
            .success(success)
            .errorMessage(errorMessage)
            .ipAddress(ipAddress)
            .description(description)
            .createdAt(LocalDateTime.now())
            .build();
    }
}