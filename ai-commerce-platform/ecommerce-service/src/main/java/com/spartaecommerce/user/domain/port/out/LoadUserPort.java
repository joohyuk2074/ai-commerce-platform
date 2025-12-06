package com.spartaecommerce.user.domain.port.out;

import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.domain.entity.User;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface LoadUserPort {

    Optional<User> findById(Long userId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User getById(Long userId);

    User getByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> search(UserSearchQuery searchQuery);
}
