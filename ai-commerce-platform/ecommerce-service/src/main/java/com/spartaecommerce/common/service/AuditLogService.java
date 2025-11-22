package com.spartaecommerce.common.service;

import com.spartaecommerce.common.domain.AuditLog;
import com.spartaecommerce.common.infrastructure.persistence.jpa.AuditLogJpaEntity;
import com.spartaecommerce.common.infrastructure.persistence.jpa.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(AuditLog auditLog) {
        try {
            AuditLogJpaEntity jpaEntity = AuditLogJpaEntity.from(auditLog);

            auditLogJpaRepository.save(jpaEntity);

            log.debug("Audit log saved: action={}, userId={}, success={}",
                auditLog.getAction(), auditLog.getUserId(), auditLog.getSuccess());
        } catch (Exception e) {
            log.error("Failed to save audit log: action={}, error={}",
                auditLog.getAction(), e.getMessage(), e);
        }
    }
}