package com.spartaecommerce.user.adapter.in.web;

import com.spartaecommerce.common.aop.Loggable;
import com.spartaecommerce.common.domain.AuditActionType;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.user.adapter.in.web.dto.request.UserSearchRequest;
import com.spartaecommerce.user.adapter.in.web.dto.request.UserUpdateRequest;
import com.spartaecommerce.user.adapter.in.web.dto.response.UserResponse;
import com.spartaecommerce.user.application.dto.command.UserUpdateCommand;
import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.domain.port.in.DeleteUserUseCase;
import com.spartaecommerce.user.domain.port.in.GetUserUseCase;
import com.spartaecommerce.user.domain.port.in.SearchUsersUseCase;
import com.spartaecommerce.user.domain.port.in.UpdateUserUseCase;
import com.spartaecommerce.util.PageResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final SearchUsersUseCase searchUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<UserResponse>> getById(
        @PathVariable Long userId
    ) {
        UserResult userResult = getUserUseCase.getById(userId);
        UserResponse response = UserResponse.from(userResult);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

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

    @PatchMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<Void>> update(
        @PathVariable Long userId,
        @RequestBody UserUpdateRequest updateRequest
    ) {
        UserUpdateCommand updateCommand = updateRequest.toCommand();
        updateUserUseCase.update(userId, updateCommand);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long userId) {
        deleteUserUseCase.delete(userId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
