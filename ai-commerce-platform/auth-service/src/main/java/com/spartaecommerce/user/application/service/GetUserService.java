package com.spartaecommerce.user.application.service;

import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.in.GetUserUseCase;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserService implements GetUserUseCase {

    private final LoadUserPort loadUserPort;

    @Override
    public UserResult getById(Long userId) {
        User user = loadUserPort.getById(userId);
        return UserResult.from(user);
    }

    @Override
    public UserResult getByUsername(String username) {
        User user = loadUserPort.getByUsername(username);
        return UserResult.from(user);
    }
}
