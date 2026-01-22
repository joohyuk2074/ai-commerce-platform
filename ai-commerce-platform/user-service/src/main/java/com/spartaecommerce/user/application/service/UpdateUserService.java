package com.spartaecommerce.user.application.service;

import com.spartaecommerce.user.application.dto.command.UserUpdateCommand;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.in.UpdateUserUseCase;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import com.spartaecommerce.user.domain.port.out.SaveUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateUserService implements UpdateUserUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;

    @Override
    @Transactional
    public void update(Long userId, UserUpdateCommand updateCommand) {
        User user = loadUserPort.getById(userId);
        user.update(updateCommand.name(), updateCommand.phoneNumber());
        saveUserPort.save(user);
    }
}
