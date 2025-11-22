package com.spartaecommerce.common.infrastructure.persistence.jpa;

import com.spartaecommerce.common.domain.AuditActionType;
import com.spartaecommerce.common.domain.AuditLog;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_logs_action", columnList = "action"),
    @Index(name = "idx_audit_logs_created_at", columnList = "created_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditLogId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuditActionType action;

    @Column
    private Long userId;

    @Column(nullable = false, length = 200)
    private String methodName;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column
    private Long executionTimeMs;

    @Column(nullable = false)
    private Boolean success;

    @Column(length = 500)
    private String errorMessage;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 200)
    private String description;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public static AuditLogJpaEntity from(AuditLog auditLog) {
        return AuditLogJpaEntity.builder()
            .auditLogId(auditLog.getAuditLogId())
            .action(auditLog.getAction())
            .userId(auditLog.getUserId())
            .methodName(auditLog.getMethodName())
            .parameters(auditLog.getParameters())
            .result(auditLog.getResult())
            .executionTimeMs(auditLog.getExecutionTimeMs())
            .success(auditLog.getSuccess())
            .errorMessage(auditLog.getErrorMessage())
            .ipAddress(auditLog.getIpAddress())
            .description(auditLog.getDescription())
            .createdAt(auditLog.getCreatedAt())
            .build();
    }

    public AuditLog toDomain() {
        return AuditLog.builder()
            .auditLogId(this.auditLogId)
            .action(this.action)
            .userId(this.userId)
            .methodName(this.methodName)
            .parameters(this.parameters)
            .result(this.result)
            .executionTimeMs(this.executionTimeMs)
            .success(this.success)
            .errorMessage(this.errorMessage)
            .ipAddress(this.ipAddress)
            .description(this.description)
            .createdAt(this.createdAt)
            .build();
    }
}