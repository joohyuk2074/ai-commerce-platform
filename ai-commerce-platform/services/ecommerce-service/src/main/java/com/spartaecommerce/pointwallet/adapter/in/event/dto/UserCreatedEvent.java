package com.spartaecommerce.pointwallet.adapter.in.event.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.spartaecommerce.common.domain.event.BaseDomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Kafka Event DTO: auth-service에서 발행한 User 생성 이벤트
 * <p>
 * 도메인 위치:
 * - PointWallet 도메인의 Input Event DTO
 * - User 도메인이 아닌 PointWallet 도메인에 속함
 * - auth-service의 UserCreatedEvent를 수신하여 PointWallet을 생성하기 위한 DTO
 * <p>
 * 주의: auth-service의 UserCreatedEvent와 동일한 구조 유지
 * (이벤트 역직렬화를 위해 필드명, 타입 일치 필수)
 */
@Getter
public class UserCreatedEvent extends BaseDomainEvent {

    private final Long userId;

    private final String username;

    private final String email;

    private final String name;

    private final String phoneNumber;

    private final String grade;

    /**
     * Jackson 역직렬화를 위한 생성자
     */
    @JsonCreator
    public UserCreatedEvent(
        @JsonProperty("userId") Long userId,
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("name") String name,
        @JsonProperty("phoneNumber") String phoneNumber,
        @JsonProperty("grade") String grade,
        @JsonProperty("occurredAt") LocalDateTime occurredAt
    ) {
        super(occurredAt);
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.grade = grade;
    }
}
