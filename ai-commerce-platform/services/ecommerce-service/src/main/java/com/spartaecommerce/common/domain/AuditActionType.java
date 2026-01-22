package com.spartaecommerce.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditActionType {

    CREATE("생성"),
    READ("조회"),
    UPDATE("수정"),
    DELETE("삭제"),
    ACTION("특수 액션"),
    UNKNOWN("알 수 없음");

    private final String description;

    public static AuditActionType fromString(String action) {
        if (action == null) {
            return UNKNOWN;
        }

        try {
            return AuditActionType.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}