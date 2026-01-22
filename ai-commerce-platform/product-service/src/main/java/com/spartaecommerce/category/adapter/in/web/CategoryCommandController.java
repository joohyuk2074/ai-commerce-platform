package com.spartaecommerce.category.adapter.in.web;

import com.spartaecommerce.category.adapter.in.web.dto.request.CategoryRegisterRequest;
import com.spartaecommerce.category.adapter.in.web.dto.request.CategoryUpdateRequest;
import com.spartaecommerce.category.application.dto.commnad.CategoryRegisterCommand;
import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;
import com.spartaecommerce.category.domain.port.in.CategoryCommandUseCase;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryCommandController {

    private final CategoryCommandUseCase categoryCommandUseCase;

    @PostMapping("/categories")
    public ResponseEntity<CommonResponse<IdResponse>> create(
        @Valid @RequestBody CategoryRegisterRequest registerRequest
    ) {
        CategoryRegisterCommand registerCommand = registerRequest.toCommand();

        Long categoryId = categoryCommandUseCase.register(registerCommand);

        return ResponseEntity
            .created(URI.create("/api/v1/categories/" + categoryId))
            .body(CommonResponse.create(categoryId));
    }

    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity<CommonResponse<Void>> update(
        @PathVariable Long categoryId,
        @Valid @RequestBody CategoryUpdateRequest updateRequest
    ) {
        CategoryUpdateCommand updateCommand = updateRequest.toCommand(categoryId);
        categoryCommandUseCase.update(updateCommand);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long categoryId) {
        categoryCommandUseCase.delete(categoryId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
