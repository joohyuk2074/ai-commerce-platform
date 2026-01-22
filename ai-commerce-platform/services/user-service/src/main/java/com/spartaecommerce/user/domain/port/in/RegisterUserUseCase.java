package com.spartaecommerce.user.domain.port.in;

import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;

public interface RegisterUserUseCase {

    Long register(RegisterUserCommand command);
}