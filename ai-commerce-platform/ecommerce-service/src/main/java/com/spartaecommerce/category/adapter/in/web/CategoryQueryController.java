package com.spartaecommerce.category.adapter.in.web;

import com.spartaecommerce.category.adapter.in.web.dto.request.CategorySearchRequest;
import com.spartaecommerce.category.adapter.in.web.dto.request.TopSellingProductsRequest;
import com.spartaecommerce.category.adapter.in.web.dto.response.CategoryProductSalesRankingResponse;
import com.spartaecommerce.category.adapter.in.web.dto.response.CategoryResponse;
import com.spartaecommerce.category.adapter.in.web.dto.response.CategoryTreeResponse;
import com.spartaecommerce.category.application.dto.query.CategorySearchQuery;
import com.spartaecommerce.category.application.dto.query.TopSellingProductsQuery;
import com.spartaecommerce.category.application.dto.result.CategoryProductSalesRankingResult;
import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;
import com.spartaecommerce.common.domain.category.Category;
import com.spartaecommerce.category.domain.port.in.CategoryQueryUseCase;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryQueryController {

    private final CategoryQueryUseCase categoryQueryUseCase;

    @GetMapping("/categories")
    public ResponseEntity<CommonResponse<PageResponse<CategoryResponse>>> search(
        CategorySearchRequest searchRequest
    ) {
        CategorySearchQuery searchQuery = searchRequest.toQuery();

        List<Category> categories = categoryQueryUseCase.search(searchQuery);
        List<CategoryResponse> response = categories.stream()
            .map(CategoryResponse::from)
            .toList();

        return ResponseEntity.ok(CommonResponse.success(PageResponse.of(response)));
    }

    @GetMapping("/categories/tree")
    public ResponseEntity<CommonResponse<CategoryTreeResponse>> getCategoryTree() {
        List<CategoryTreeNodeResult> categoryTree = categoryQueryUseCase.getCategoryTree();
        CategoryTreeResponse categoryTreeResponse = new CategoryTreeResponse(categoryTree);
        return ResponseEntity.ok(CommonResponse.success(categoryTreeResponse));
    }

    @GetMapping("/categories/top-selling")
    public ResponseEntity<CommonResponse<CategoryProductSalesRankingResponse>> getTopSellingProductsByCategory(
        @Valid @ModelAttribute TopSellingProductsRequest request
    ) {
        TopSellingProductsQuery query = request.toQuery();
        List<CategoryProductSalesRankingResult> results = categoryQueryUseCase.getTopSellingProductsByCategory(query);
        CategoryProductSalesRankingResponse response = CategoryProductSalesRankingResponse.from(results);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
