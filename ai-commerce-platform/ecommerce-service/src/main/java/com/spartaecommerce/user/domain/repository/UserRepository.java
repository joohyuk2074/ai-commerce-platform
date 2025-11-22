package com.spartaecommerce.user.domain.repository;

import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.domain.entity.User;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserRepository {

    Long save(User user);

    Optional<User> findById(Long userId);

    User getById(Long userId);

    boolean existsById(Long userId);

    boolean existsByEmail(String email);

    Page<User> search(UserSearchQuery searchQuery);
}
