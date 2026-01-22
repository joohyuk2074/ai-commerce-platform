package com.spartaecommerce.user.domain.port.out;

import com.spartaecommerce.user.domain.event.UserCreatedEvent;

/**
 * Port: User 도메인 이벤트 발행
 * Hexagonal Architecture - Output Port
 */
public interface UserEventPublisher {

    /**
     * User 생성 이벤트 발행
     * @param event User 생성 이벤트
     */
    void publishUserCreated(UserCreatedEvent event);
}
