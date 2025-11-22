package com.spartaecommerce.user.domain.repository;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UserFakeRepository implements UserRepository {

    private final Map<Long, User> repository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(User user) {
        if (user.getUserId() == null) {
            long userId = idGenerator.getAndIncrement();
            User newUser = User.builder()
                .userId(userId)
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .grade(user.getGrade())
                .deleted(user.isDeleted())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(userId, newUser);
            return userId;
        } else {
            User updatedUser = User.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .grade(user.getGrade())
                .deleted(user.isDeleted())
                .createdAt(user.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(user.getUserId(), updatedUser);
            return user.getUserId();
        }
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(repository.get(userId))
            .filter(user -> !user.isDeleted());
    }

    @Override
    public User getById(Long userId) {
        return findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "userId: " + userId));
    }

    @Override
    public boolean existsById(Long userId) {
        return repository.containsKey(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.values().stream()
            .filter(user -> !user.isDeleted())
            .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public Page<User> search(UserSearchQuery searchQuery) {
        List<User> filteredUsers = repository.values().stream()
            .filter(user -> !user.isDeleted())
            .filter(user -> searchQuery.email() == null || user.getEmail().contains(searchQuery.email()))
            .filter(user -> searchQuery.name() == null || user.getName().contains(searchQuery.name()))
            .toList();

        PageRequest pageRequest = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filteredUsers.size());

        List<User> pageContent = filteredUsers.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, filteredUsers.size());
    }

    public void clear() {
        repository.clear();
    }
}