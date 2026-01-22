package com.spartaecommerce.user.domain.event;

import com.spartaecommerce.common.domain.event.BaseDomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Domain Event: User 생성 이벤트
 * <p>
 * Event-Driven Architecture:
 * - Producer: auth-service (User 생성 시)
 * - Consumer: ecommerce-service (PointWallet 자동 생성)
 * - Topic: ecommerce.event.user.v1
 * <p>
 * 이벤트 필드 설계 원칙:
 * - 도메인 객체(User) 직접 포함 X → 결합도 증가 방지
 * - 필요한 필드만 명시적으로 포함
 * - 민감 정보(password) 제외
 * - Consumer가 추가 정보 필요 시 API 호출
 */
@Getter
public class UserCreatedEvent extends BaseDomainEvent {

    private Long userId;

    private String username;

    private String email;

    private String name;

    private String phoneNumber;

    private String grade;

    // Jackson을 위한 기본 생성자
    protected UserCreatedEvent() {
        super();
    }

    private UserCreatedEvent(
        Long userId,
        String username,
        String email,
        String name,
        String phoneNumber,
        String grade,
        LocalDateTime occurredAt
    ) {
        super(1, occurredAt);  // schemaVersion = 1
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.grade = grade;
    }

    public static UserCreatedEvent create(
        Long userId,
        String username,
        String email,
        String name,
        String phoneNumber,
        String grade,
        LocalDateTime occurredAt
    ) {
        return new UserCreatedEvent(
            userId,
            username,
            email,
            name,
            phoneNumber,
            grade,
            occurredAt
        );
    }
}
