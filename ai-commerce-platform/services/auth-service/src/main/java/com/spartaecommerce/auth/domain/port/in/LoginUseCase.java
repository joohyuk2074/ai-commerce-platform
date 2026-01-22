package com.spartaecommerce.auth.domain.port.in;

import com.spartaecommerce.auth.dto.LoginRequest;
import com.spartaecommerce.auth.dto.LoginResponse;

public interface LoginUseCase {

    LoginResponse login(LoginRequest request);
}
