package com.spartaecommerce.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Passport 빌더
 *
 * Passport 객체를 쉽게 생성하기 위한 빌더 패턴 구현
 */
public class PassportBuilder {

    private Long userId;
    private String username;
    private String email;
    private String grade;
    private List<String> roles = new ArrayList<>();
    private Map<String, Object> metadata = new HashMap<>();
    private Instant issuedAt = Instant.now();
    private Instant expiresAt = Instant.now().plus(Duration.ofHours(1));
    private String passportId = UUID.randomUUID().toString();

    public PassportBuilder userId(Long userId) {
        this.userId = userId;
        return this;
    }

    public PassportBuilder username(String username) {
        this.username = username;
        return this;
    }

    public PassportBuilder email(String email) {
        this.email = email;
        return this;
    }

    public PassportBuilder grade(String grade) {
        this.grade = grade;
        return this;
    }

    public PassportBuilder roles(List<String> roles) {
        this.roles = new ArrayList<>(roles);
        return this;
    }

    public PassportBuilder addRole(String role) {
        this.roles.add(role);
        return this;
    }

    public PassportBuilder metadata(Map<String, Object> metadata) {
        this.metadata = new HashMap<>(metadata);
        return this;
    }

    public PassportBuilder addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    public PassportBuilder issuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
        return this;
    }

    public PassportBuilder expiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public PassportBuilder ttl(Duration ttl) {
        this.expiresAt = this.issuedAt.plus(ttl);
        return this;
    }

    public PassportBuilder passportId(String passportId) {
        this.passportId = passportId;
        return this;
    }

    public Passport build() {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(grade, "grade must not be null");

        return new Passport(
            userId,
            username,
            email,
            grade,
            roles,
            metadata,
            issuedAt,
            expiresAt,
            passportId
        );
    }

    public static PassportBuilder builder() {
        return new PassportBuilder();
    }
}
