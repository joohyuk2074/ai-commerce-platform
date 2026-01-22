package com.spartaecommerce.user.adapter.in.web;

import com.spartaecommerce.auth.domain.port.in.CreatePassportUseCase;
import com.spartaecommerce.auth.domain.port.in.LoginUseCase;
import com.spartaecommerce.auth.domain.port.in.RefreshTokenUseCase;
import com.spartaecommerce.auth.domain.port.in.ValidateTokenUseCase;
import com.spartaecommerce.auth.dto.LoginRequest;
import com.spartaecommerce.auth.dto.LoginResponse;
import com.spartaecommerce.auth.dto.RefreshTokenRequest;
import com.spartaecommerce.common.auth.Passport;
import com.spartaecommerce.common.domain.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final CreatePassportUseCase createPassportUseCase;

    @Operation(summary = "로그인", description = "사용자 로그인 및 JWT 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = loginUseCase.login(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token 발급")
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<LoginResponse>> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        LoginResponse response = refreshTokenUseCase.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(
        summary = "JWT 토큰 검증 (Internal)",
        description = "API Gateway에서 사용하는 JWT 토큰 검증 API. Authorization 헤더의 Bearer 토큰을 검증합니다."
    )
    @PostMapping("/validate")
    public ResponseEntity<CommonResponse<TokenValidationResponse>> validateToken(
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        TokenValidationResponse response = validateTokenUseCase.validateToken(authorizationHeader);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(
        summary = "Passport 생성 (Internal)",
        description = "API Gateway에서 사용하는 Passport 생성 API. JWT 토큰으로부터 Passport를 생성합니다."
    )
    @PostMapping("/passport")
    public ResponseEntity<CommonResponse<Passport>> createPassport(
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        // Bearer prefix 제거
        String token = authorizationHeader.startsWith("Bearer ")
            ? authorizationHeader.substring(7)
            : authorizationHeader;

        Passport passport = createPassportUseCase.createPassportFromToken(token);
        return ResponseEntity.ok(CommonResponse.success(passport));
    }

    /**
     * 토큰 검증 응답 DTO
     */
    public record TokenValidationResponse(
        boolean valid,
        UserInfo userInfo,
        String message
    ) {
        public record UserInfo(
            Long userId,
            String username,
            java.util.List<String> roles
        ) {}

        public static TokenValidationResponse valid(Long userId, String username, java.util.List<String> roles) {
            return new TokenValidationResponse(
                true,
                new UserInfo(userId, username, roles),
                "Token is valid"
            );
        }

        public static TokenValidationResponse invalid(String message) {
            return new TokenValidationResponse(false, null, message);
        }
    }
}
