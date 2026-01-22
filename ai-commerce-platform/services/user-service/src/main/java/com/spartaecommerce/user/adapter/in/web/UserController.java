package com.spartaecommerce.user.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.user.adapter.in.web.dto.RegisterRequest;
import com.spartaecommerce.user.adapter.in.web.dto.request.UserSearchRequest;
import com.spartaecommerce.user.adapter.in.web.dto.request.UserUpdateRequest;
import com.spartaecommerce.user.adapter.in.web.dto.response.UserResponse;
import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;
import com.spartaecommerce.user.application.dto.command.UserUpdateCommand;
import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.domain.port.in.DeleteUserUseCase;
import com.spartaecommerce.user.domain.port.in.GetUserUseCase;
import com.spartaecommerce.user.domain.port.in.RegisterUserUseCase;
import com.spartaecommerce.user.domain.port.in.SearchUsersUseCase;
import com.spartaecommerce.user.domain.port.in.UpdateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.spartaecommerce.util.PageResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final SearchUsersUseCase searchUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @PostMapping("/users/register")
    public ResponseEntity<CommonResponse<IdResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        RegisterUserCommand command = request.toCommand();
        Long userId = registerUserUseCase.register(command);
        CommonResponse<IdResponse> idResponseCommonResponse = CommonResponse.create(userId);

        return ResponseEntity.ok(idResponseCommonResponse);
    }

    @Operation(summary = "사용자 조회", description = "사용자 ID로 사용자 정보를 조회합니다")
    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<UserResponse>> getById(
        @PathVariable Long userId
    ) {
        UserResult userResult = getUserUseCase.getById(userId);
        UserResponse response = UserResponse.from(userResult);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "사용자 검색", description = "조건에 맞는 사용자 목록을 조회합니다")
    @GetMapping("/users")
    public ResponseEntity<CommonResponse<PageResponse<UserResponse>>> search(
        UserSearchRequest searchRequest
    ) {
        UserSearchQuery searchQuery = searchRequest.toQuery();
        Page<UserResult> userResults = searchUsersUseCase.search(searchQuery);
        Page<UserResponse> productResponse = userResults.map(UserResponse::from);

        PageResponse<UserResponse> data = PageResponseMapper.of(productResponse);

        return ResponseEntity.ok(CommonResponse.success(data));
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자의 정보를 수정합니다")
    @PatchMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<Void>> update(
        @PathVariable Long userId,
        @RequestBody UserUpdateRequest updateRequest
    ) {
        UserUpdateCommand updateCommand = updateRequest.toCommand();
        updateUserUseCase.update(userId, updateCommand);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다 (소프트 삭제)")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long userId) {
        deleteUserUseCase.delete(userId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
