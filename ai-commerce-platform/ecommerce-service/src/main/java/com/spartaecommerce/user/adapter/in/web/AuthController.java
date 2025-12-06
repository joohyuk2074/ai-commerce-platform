package com.spartaecommerce.user.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.user.adapter.in.web.dto.RegisterRequest;
import com.spartaecommerce.user.adapter.in.web.dto.response.UserResponse;
import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;
import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.domain.port.in.GetUserUseCase;
import com.spartaecommerce.user.domain.port.in.RegisterUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetUserUseCase getUserUseCase;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<IdResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        RegisterUserCommand command = request.toCommand();
        Long userId = registerUserUseCase.register(command);
        CommonResponse<IdResponse> idResponseCommonResponse = CommonResponse.create(userId);

        return ResponseEntity.ok(idResponseCommonResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        String name = authentication.getName();
        UserResult userResult = getUserUseCase.getByUsername(name);
        CommonResponse<UserResponse> response = CommonResponse.success(UserResponse.from(userResult));

        return ResponseEntity.ok(response);
    }
}
