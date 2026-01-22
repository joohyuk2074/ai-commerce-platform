package com.spartaecommerce.common.infrastructure.lock.aspect;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.common.infrastructure.lock.DistributedLock;
import com.spartaecommerce.common.infrastructure.lock.manager.DistributedLockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

    private final DistributedLockManager lockManager;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object handleDistributedLock(
        ProceedingJoinPoint joinPoint,
        DistributedLock distributedLock
    ) throws Throwable {
        String lockKey = parseLockKey(distributedLock.key(), joinPoint);

        log.info("Attempting to acquire lock: {}", lockKey);

        boolean acquired = lockManager.tryLock(
            lockKey,
            distributedLock.waitTime(),
            distributedLock.leaseTime()
        );

        if (!acquired) {
            log.warn("Failed to acquire lock: {}", lockKey);
            // TODO: 예외 변경
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Lock Error");
        }

        try {
            log.info("Lock acquired successfully: {}", lockKey);
            return joinPoint.proceed();
        } finally {
            lockManager.unlock(lockKey);
            log.info("Lock released: {}", lockKey);
        }
    }

    private String parseLockKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        Expression expression = parser.parseExpression(keyExpression);
        return expression.getValue(context, String.class);
    }
}
