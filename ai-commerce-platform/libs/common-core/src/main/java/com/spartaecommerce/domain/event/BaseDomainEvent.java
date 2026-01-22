package com.spartaecommerce.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 모든 도메인 이벤트의 베이스 클래스
 * <p>
 * Event-Driven Architecture의 핵심 패턴:
 * - 이벤트 추적성: eventId, occurredAt
 * - 버전 관리: schemaVersion
 * - 메타데이터: 추적 및 디버깅 정보
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
@JsonDeserialize
public abstract class BaseDomainEvent {

    /**
     * 이벤트 고유 식별자 (UUID)
     * 용도: 이벤트 추적, 중복 방지, 로깅
     */
    private String eventId;

    /**
     * 이벤트 타입 (클래스명 자동 설정)
     * 예: "UserCreatedEvent", "OrderPlacedEvent"
     */
    private String eventType;

    /**
     * 이벤트 발생 시각
     * 용도: 순서 보장, 타임라인 분석
     */
    private LocalDateTime occurredAt;

    /**
     * 이벤트 스키마 버전
     * 용도: 호환성 관리, 마이그레이션
     * 기본값: 1
     */
    private int schemaVersion;

    /**
     * 메타데이터 (선택적)
     * 예: correlationId, userId, sessionId, source 등
     */
    private Map<String, String> metadata;

    // Jackson을 위한 기본 생성자
    protected BaseDomainEvent() {
        this.metadata = new HashMap<>();
    }

    protected BaseDomainEvent(LocalDateTime occurredAt) {
        this(1, occurredAt);
    }

    protected BaseDomainEvent(int schemaVersion, LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.occurredAt = occurredAt;
        this.schemaVersion = schemaVersion;
        this.metadata = new HashMap<>();
    }

    /**
     * 메타데이터 추가
     * 용도: Correlation ID, Trace ID 등 추적 정보
     */
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    /**
     * 메타데이터 조회
     */
    public String getMetadata(String key) {
        return this.metadata.get(key);
    }

    /**
     * 이벤트 요약 정보 (로깅용)
     */
    @Override
    public String toString() {
        return String.format("%s[eventId=%s, occurredAt=%s, version=%d]",
            eventType, eventId, occurredAt, schemaVersion);
    }
}
