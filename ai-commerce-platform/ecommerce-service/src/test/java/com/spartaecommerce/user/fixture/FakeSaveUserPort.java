package com.spartaecommerce.user.fixture;

import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.SaveUserPort;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트용 SaveUserPort Fake 구현체
 * 메모리 기반으로 사용자 저장 기능을 제공합니다.
 */
public class FakeSaveUserPort implements SaveUserPort {
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final FakeLoadUserPort loadUserPort;

    public FakeSaveUserPort(FakeLoadUserPort loadUserPort) {
        this.loadUserPort = loadUserPort;
    }

    @Override
    public Long save(User user) {
        Long userId = user.getUserId();
        if (userId == null) {
            userId = idGenerator.getAndIncrement();
        }

        User savedUser = User.builder()
            .userId(userId)
            .username(user.getUsername())
            .password(user.getPassword())
            .email(user.getEmail())
            .name(user.getName())
            .phoneNumber(user.getPhoneNumber())
            .grade(user.getGrade())
            .deleted(user.isDeleted())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();

        loadUserPort.addUser(savedUser);
        return userId;
    }
}
