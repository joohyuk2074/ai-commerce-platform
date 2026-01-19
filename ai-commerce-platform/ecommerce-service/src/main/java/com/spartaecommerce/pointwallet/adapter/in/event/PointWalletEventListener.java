package com.spartaecommerce.pointwallet.adapter.in.event;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.pointwallet.adapter.in.event.dto.UserCreatedEvent;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.port.out.SavePointWalletPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka Event Listener: auth-service의 User 생성 이벤트 수신 및 PointWallet 생성
 * Hexagonal Architecture: Input Adapter (PointWallet 도메인)
 * <p>
 * MSA Pattern: Event-Driven Architecture
 * - auth-service에서 User 생성 시 이벤트 발행
 * - ecommerce-service의 PointWallet 도메인에서 이벤트 수신하여 PointWallet 초기화
 * - Eventual Consistency 패턴 적용
 * <p>
 * 도메인 위치:
 * - 이 리스너는 PointWallet 도메인에 속함 (User 도메인 아님)
 * - UserCreatedEvent를 수신하지만, PointWallet을 생성하는 것이 핵심 책임
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PointWalletEventListener {

    private final SavePointWalletPort savePointWalletPort;

    /**
     * User 생성 이벤트를 수신하여 PointWallet 초기화
     * - PointWallet 생성 (잔액 0원)
     * - Manual Commit으로 트랜잭션 보장
     *
     * @param event          auth-service에서 발행한 User 생성 이벤트
     * @param acknowledgment Kafka manual acknowledgment
     */
    @KafkaListener(
        topics = "ecommerce.event.user.v1",
        groupId = "ecommerce-service",
        containerFactory = "userEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleUserCreated(UserCreatedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received UserCreatedEvent: userId={}, username={}, email={}",
                event.getUserId(), event.getUsername(), event.getEmail());

            // PointWallet 생성
            PointWallet wallet = PointWallet.createNew(event.getUserId());
            savePointWalletPort.save(wallet);

            log.info("PointWallet created successfully for userId: {}, balance: {}",
                event.getUserId(), wallet.getBalance().amount());

            // Kafka manual commit
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process UserCreatedEvent: userId={}, error={}",
                event.getUserId(), e.getMessage(), e);
            // 에러 발생 시 commit하지 않음 -> 재처리됨
            throw new BusinessException("Failed to process UserEvent", e, ErrorCode.MESSAGE_CONSUME_ERROR);
        }
    }
}
