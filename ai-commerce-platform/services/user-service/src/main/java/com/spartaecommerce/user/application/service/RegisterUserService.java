package com.spartaecommerce.user.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.event.UserCreatedEvent;
import com.spartaecommerce.user.domain.port.in.RegisterUserUseCase;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import com.spartaecommerce.user.domain.port.out.PasswordEncoderPort;
import com.spartaecommerce.user.domain.port.out.SaveUserPort;
import com.spartaecommerce.user.domain.port.out.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegisterUserService implements RegisterUserUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final UserEventPublisher userEventPublisher;

    @Override
    @Transactional
    public Long register(RegisterUserCommand command) {
        if (loadUserPort.existsByUsername(command.username())) {
            throw new BusinessException(
                ErrorCode.ENTITY_ALREADY_EXISTS,
                "Username already exists: " + command.username()
            );
        }

        if (loadUserPort.existsByEmail(command.email())) {
            throw new BusinessException(
                ErrorCode.ENTITY_ALREADY_EXISTS,
                "Email already exists: " + command.email()
            );
        }

        String encodedPassword = passwordEncoderPort.encode(command.password());

        User user = User.createNew(
            command.username(),
            encodedPassword,
            command.email(),
            command.name(),
            command.phoneNumber()
        );

        Long userId = saveUserPort.save(user);

        log.info("User {} has been created", userId);

        // User 생성 이벤트 발행 (ecommerce-service에서 PointWallet 생성)
        // BaseDomainEvent 상속으로 eventId, occurredAt 자동 생성
        UserCreatedEvent event = UserCreatedEvent.create(
            userId,
            command.username(),
            command.email(),
            command.name(),
            command.phoneNumber(),
            user.getGrade().name(),
            LocalDateTime.now()
        );

        userEventPublisher.publishUserCreated(event);
        log.info("UserCreatedEvent published: eventId={}, userId={}, eventType={}",
            event.getEventId(), userId, event.getEventType());

        return userId;
    }
}
