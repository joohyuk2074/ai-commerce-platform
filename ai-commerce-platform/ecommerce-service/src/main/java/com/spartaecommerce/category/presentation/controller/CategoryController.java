package com.spartaecommerce.category.presentation.controller;

import com.spartaecommerce.category.application.CategoryService;
import com.spartaecommerce.category.application.dto.commnad.CategoryRegisterCommand;
import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.application.dto.query.CategorySearchQuery;
import com.spartaecommerce.category.presentation.controller.dto.request.CategoryRegisterRequest;
import com.spartaecommerce.category.presentation.controller.dto.request.CategorySearchRequest;
import com.spartaecommerce.category.presentation.controller.dto.request.CategoryUpdateRequest;
import com.spartaecommerce.category.presentation.controller.dto.response.CategoryResponse;
import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;
import com.spartaecommerce.category.presentation.controller.dto.response.CategoryTreeResponse;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.common.domain.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/categories")
    public ResponseEntity<CommonResponse<IdResponse>> create(
        @Valid @RequestBody CategoryRegisterRequest registerRequest
    ) {
        CategoryRegisterCommand registerCommand = registerRequest.toCommand();

        Long categoryId = categoryService.register(registerCommand);

        return ResponseEntity
            .created(URI.create("/api/v1/categories/" + categoryId))
            .body(CommonResponse.create(categoryId));
    }

    @GetMapping("/categories")
    public ResponseEntity<CommonResponse<PageResponse<CategoryResponse>>> search(
        CategorySearchRequest searchRequest
    ) {
        CategorySearchQuery searchQuery = searchRequest.toQuery();

        List<Category> products = categoryService.search(searchQuery);
        List<CategoryResponse> response = products.stream()
            .map(CategoryResponse::from)
            .toList();

        return ResponseEntity.ok(CommonResponse.success(PageResponse.of(response)));
    }

    @GetMapping("/categories/tree")
    public ResponseEntity<CommonResponse<CategoryTreeResponse>> getCategoryTree() {
        List<CategoryTreeNodeResult> categoryTree = categoryService.getCategoryTree();
        CategoryTreeResponse categoryTreeResponse = new CategoryTreeResponse(categoryTree);
        return ResponseEntity.ok(CommonResponse.success(categoryTreeResponse));
    }

    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity<CommonResponse<Void>> update(
        @PathVariable Long categoryId,
        @RequestBody CategoryUpdateRequest updateRequest
    ) {
        CategoryUpdateCommand updateCommand = updateRequest.toCommand();
        categoryService.update(categoryId, updateCommand);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
