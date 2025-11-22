package com.spartaecommerce.common.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.common.domain.AuditActionType;
import com.spartaecommerce.common.domain.AuditLog;
import com.spartaecommerce.common.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * AOP 기반 감사 로그 Aspect
 *
 * @Loggable 어노테이션이 붙은 메서드 또는 클래스의 모든 public 메서드를 로깅합니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    /**
     * @Loggable 어노테이션이 붙은 메서드
     */
    @Pointcut("@annotation(com.spartaecommerce.common.aop.Loggable)")
    public void loggableMethod() {
    }

    /**
     * @Loggable 어노테이션이 붙은 클래스의 모든 public 메서드
     */
    @Pointcut("@within(com.spartaecommerce.common.aop.Loggable) && execution(public * *(..))")
    public void loggableClass() {
    }

    /**
     * 사용자 관련 생성/수정 메서드 포인트컷 (공통 패턴)
     * - UserController의 create, update 메서드
     * - 다른 도메인도 필요 시 추가 가능
     */
    @Pointcut("execution(* com.spartaecommerce.user.presentation.controller.UserController.create*(..)) || " +
        "execution(* com.spartaecommerce.user.presentation.controller.UserController.update*(..))")
    public void userModificationMethods() {
    }

    /**
     * @Loggable 어노테이션이 붙은 메서드 또는 클래스, 또는 사용자 수정 메서드를 감사 로그로 기록
     */
    @Around("loggableMethod() || loggableClass() || userModificationMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // @Loggable 어노테이션 정보 추출
        Loggable loggable = method.getAnnotation(Loggable.class);
        if (loggable == null) {
            // 클래스 레벨 어노테이션 확인
            loggable = joinPoint.getTarget().getClass().getAnnotation(Loggable.class);
        }

        // 기본값 설정 (어노테이션이 없는 경우 - userModificationMethods 포인트컷)
        AuditActionType action = loggable != null && loggable.action() != AuditActionType.UNKNOWN
            ? loggable.action()
            : extractActionFromMethodName(method.getName());
        String description = loggable != null ? loggable.description() : "";
        boolean logParameters = loggable == null || loggable.logParameters();
        boolean logResult = loggable != null && loggable.logResult();

        String methodName = joinPoint.getSignature().toShortString();
        String parameters = null;
        String result = null;
        boolean success = true;
        String errorMessage = null;
        Long userId = null;
        String ipAddress = null;

        try {
            if (logParameters) {
                parameters = serializeParameters(joinPoint.getArgs());
            }

            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                ipAddress = getClientIpAddress(request);
                userId = extractUserIdFromRequest(request);
            }

            // 실제 메서드 실행
            Object returnValue = joinPoint.proceed();

            // 응답 결과 로깅
            if (logResult && returnValue != null) {
                result = serializeObject(returnValue);
            }

            return returnValue;

        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > 500) {
                errorMessage = errorMessage.substring(0, 500);
            }
            throw e;

        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            // 감사 로그 생성 및 저장 (비동기)
            AuditLog auditLog = AuditLog.createNew(
                action,
                userId,
                methodName,
                parameters,
                result,
                executionTime,
                success,
                errorMessage,
                ipAddress,
                description
            );

            auditLogService.saveAuditLog(auditLog);

            // 콘솔 로그
            log.info("[AUDIT] action={}, userId={}, method={}, executionTime={}ms, success={}",
                action, userId, methodName, executionTime, success);
        }
    }

    private AuditActionType extractActionFromMethodName(String methodName) {
        if (methodName.startsWith("create")) {
            return AuditActionType.CREATE;
        } else if (methodName.startsWith("update")) {
            return AuditActionType.UPDATE;
        } else if (methodName.startsWith("delete")) {
            return AuditActionType.DELETE;
        }
        return AuditActionType.UNKNOWN;
    }

    private String serializeParameters(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize parameters", e);
            return "[Serialization failed]";
        }
    }

    private String serializeObject(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize result", e);
            return "[Serialization failed]";
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (IllegalStateException e) {
            // 비 HTTP 컨텍스트에서 실행되는 경우
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        return Long.valueOf(userId);
    }
}