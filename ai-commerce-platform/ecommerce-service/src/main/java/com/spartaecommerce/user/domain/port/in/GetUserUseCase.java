package com.spartaecommerce.user.domain.port.in;

import com.spartaecommerce.user.application.dto.result.UserResult;

public interface GetUserUseCase {

    UserResult getById(Long userId);

    UserResult getByUsername(String username);
}
