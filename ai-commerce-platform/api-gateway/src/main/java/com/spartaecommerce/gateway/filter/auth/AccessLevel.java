package com.spartaecommerce.gateway.filter.auth;

import lombok.Getter;

@Getter
public enum AccessLevel {
    /**
     * 인증 불필요 (누구나 접근 가능)
     */
    PUBLIC("public"),

    /**
     * 조회는 public, 쓰기는 admin 필요
     */
    PUBLIC_READ_ADMIN_WRITE("public-read-admin-write"),

    /**
     * 인증 필수
     */
    AUTHENTICATED("authenticated"),

    /**
     * 관리자 권한 필요
     */
    ADMIN("admin");

    private final String level;

    AccessLevel(String level) {
        this.level = level;
    }

    public static AccessLevel fromString(String level) {
        if (level == null) {
            return PUBLIC;
        }
        for (AccessLevel accessLevel : values()) {
            if (accessLevel.level.equalsIgnoreCase(level)) {
                return accessLevel;
            }
        }
        return PUBLIC;
    }
}
