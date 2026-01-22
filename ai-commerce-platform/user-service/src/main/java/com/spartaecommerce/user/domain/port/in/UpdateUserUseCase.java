package com.spartaecommerce.user.domain.port.in;

import com.spartaecommerce.user.application.dto.command.UserUpdateCommand;

public interface UpdateUserUseCase {

    void update(Long userId, UserUpdateCommand updateCommand);
}
