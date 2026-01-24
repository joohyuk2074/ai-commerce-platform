package com.spartaecommerce.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Passport - 사용자 인증 정보를 담는 일회성 토큰
 * <p>
 * Gateway에서 Auth Service로부터 생성받아
 * 다운스트림 서비스로 전파하는 사용자 정보 컨테이너입니다.
 * <p>
 * 특징:
 * - 일회성 토큰 (트랜잭션 단위)
 * - 서버 간 전파 (헤더를 통해)
 * - 유저 정보 포함 (중복 조회 제거)
 * - 짧은 유효기간 (1시간)
 *
 * @param userId     사용자 ID
 * @param username   사용자명 (이메일)
 * @param email      이메일
 * @param grade      사용자 등급 (BRONZE, SILVER, GOLD, VIP)
 * @param roles      권한 목록
 * @param metadata   추가 메타데이터 (확장 가능)
 * @param issuedAt   Passport 발급 시간
 * @param expiresAt  Passport 만료 시간
 * @param passportId Passport 고유 ID (추적용)
 */
public record Passport(
    Long userId,
    String username,
    String email,
    String grade,
    List<String> roles,
    Map<String, Object> metadata,
    Instant issuedAt,
    Instant expiresAt,
    String passportId
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonCreator
    public Passport(
        @JsonProperty("userId") Long userId,
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("grade") String grade,
        @JsonProperty("roles") List<String> roles,
        @JsonProperty("metadata") Map<String, Object> metadata,
        @JsonProperty("issuedAt") Instant issuedAt,
        @JsonProperty("expiresAt") Instant expiresAt,
        @JsonProperty("passportId") String passportId
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.grade = grade;
        this.roles = roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Collections.emptyMap();
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.passportId = passportId;
    }

    @JsonIgnore
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    @JsonIgnore
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    @JsonIgnore
    public boolean isAdmin() {
        return roles.stream().anyMatch(role ->
            role.equals("ADMIN") || role.equals("SUPER_ADMIN")
        );
    }
}
