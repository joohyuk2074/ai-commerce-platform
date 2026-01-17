package com.spartaecommerce.user.fixture;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 테스트용 LoadUserPort Fake 구현체
 * 메모리 기반으로 사용자 조회 기능을 제공합니다.
 */
public class FakeLoadUserPort implements LoadUserPort {
    private final Map<Long, User> usersById = new HashMap<>();
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    @Override
    public User getById(Long userId) {
        return findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "User not found: " + userId));
    }

    @Override
    public User getByUsername(String username) {
        return findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "User not found: " + username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return usersByUsername.containsKey(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return usersByEmail.containsKey(email);
    }

    @Override
    public Page<User> search(UserSearchQuery searchQuery) {
        throw new UnsupportedOperationException("Not implemented for test");
    }

    /**
     * 테스트에서 사용자를 추가하기 위한 헬퍼 메서드
     */
    public void addUser(User user) {
        if (user.getUserId() != null) {
            usersById.put(user.getUserId(), user);
        }
        if (user.getUsername() != null) {
            usersByUsername.put(user.getUsername(), user);
        }
        if (user.getEmail() != null) {
            usersByEmail.put(user.getEmail(), user);
        }
    }
}
