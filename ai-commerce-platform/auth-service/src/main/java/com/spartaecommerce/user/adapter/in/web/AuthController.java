package com.spartaecommerce.user.adapter.in.web;

import com.spartaecommerce.auth.dto.LoginRequest;
import com.spartaecommerce.auth.dto.LoginResponse;
import com.spartaecommerce.auth.dto.RefreshTokenRequest;
import com.spartaecommerce.auth.service.AuthenticationService;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.user.adapter.in.web.dto.RegisterRequest;
import com.spartaecommerce.user.adapter.in.web.dto.response.UserResponse;
import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;
import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.domain.port.in.GetUserUseCase;
import com.spartaecommerce.user.domain.port.in.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final AuthenticationService authenticationService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<IdResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        RegisterUserCommand command = request.toCommand();
        Long userId = registerUserUseCase.register(command);
        CommonResponse<IdResponse> idResponseCommonResponse = CommonResponse.create(userId);

        return ResponseEntity.ok(idResponseCommonResponse);
    }

    @Operation(summary = "로그인", description = "사용자 로그인 및 JWT 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token 발급")
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<LoginResponse>> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        LoginResponse response = authenticationService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "현재 사용자 조회", description = "인증된 사용자의 정보를 조회합니다")
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        String name = authentication.getName();
        UserResult userResult = getUserUseCase.getByUsername(name);
        CommonResponse<UserResponse> response = CommonResponse.success(UserResponse.from(userResult));

        return ResponseEntity.ok(response);
    }
}
