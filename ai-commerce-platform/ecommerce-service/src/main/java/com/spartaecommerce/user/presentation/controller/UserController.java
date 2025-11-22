package com.spartaecommerce.user.presentation.controller;

import com.spartaecommerce.common.aop.Loggable;
import com.spartaecommerce.common.domain.AuditActionType;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.user.application.UserService;
import com.spartaecommerce.user.application.dto.command.UserCreateCommand;
import com.spartaecommerce.user.application.dto.command.UserUpdateCommand;
import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.presentation.controller.dto.request.UserCreateRequest;
import com.spartaecommerce.user.presentation.controller.dto.request.UserSearchRequest;
import com.spartaecommerce.user.presentation.controller.dto.request.UserUpdateRequest;
import com.spartaecommerce.user.presentation.controller.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Loggable(action = AuditActionType.CREATE, description = "새로운 사용자 생성", logParameters = true)
    @PostMapping("/users")
    public ResponseEntity<CommonResponse<IdResponse>> create(
        @Valid @RequestBody UserCreateRequest createRequest
    ) {
        UserCreateCommand createCommand = createRequest.toCommand();

        Long userId = userService.create(createCommand);

        return ResponseEntity
            .created(URI.create("/api/v1/users/" + userId))
            .body(CommonResponse.create(userId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<UserResponse>> getById(
        @PathVariable Long userId
    ) {
        UserResult userResult = userService.getById(userId);
        UserResponse response = UserResponse.from(userResult);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/users")
    public ResponseEntity<CommonResponse<PageResponse<UserResponse>>> search(
        UserSearchRequest searchRequest
    ) {
        UserSearchQuery searchQuery = searchRequest.toQuery();
        Page<UserResult> userResults = userService.search(searchQuery);
        Page<UserResponse> productResponse = userResults.map(UserResponse::from);

        PageResponse<UserResponse> data = PageResponse.of(productResponse);

        return ResponseEntity.ok(CommonResponse.success(data));
    }

    @Loggable(action = AuditActionType.UPDATE, description = "사용자 정보 수정", logParameters = true)
    @PatchMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<Void>> update(
        @PathVariable Long userId,
        @RequestBody UserUpdateRequest updateRequest
    ) {
        UserUpdateCommand updateCommand = updateRequest.toCommand();
        userService.update(userId, updateCommand);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
