package com.spartaecommerce.common.aop;

import com.spartaecommerce.common.domain.AuditActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {

    /**
     * 액션 타입 (CREATE, UPDATE, DELETE 등)
     * UNKNOWN이면 메서드명에서 자동 추출
     */
    AuditActionType action() default AuditActionType.UNKNOWN;

    /**
     * 로그 설명
     */
    String description() default "";

    /**
     * 요청 파라미터를 로그에 포함할지 여부
     */
    boolean logParameters() default true;

    /**
     * 응답 결과를 로그에 포함할지 여부
     */
    boolean logResult() default false;
}