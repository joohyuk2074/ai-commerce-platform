package com.spartaecommerce.product.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.product.adapter.in.web.dto.request.ProductSearchRequest;
import com.spartaecommerce.product.adapter.in.web.dto.response.ProductResponse;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.application.dto.result.ProductResult;
import com.spartaecommerce.product.domain.port.in.ProductQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductQueryUseCase productQueryUseCase;

    @GetMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> getProduct(
        @PathVariable Long productId
    ) {
        ProductResult productResult = productQueryUseCase.getProduct(productId);
        ProductResponse productResponse = ProductResponse.from(productResult);

        CommonResponse<ProductResponse> commonResponse = CommonResponse.success(productResponse);
        return ResponseEntity.ok(commonResponse);
    }

    @GetMapping("/products")
    public ResponseEntity<CommonResponse<PageResponse<ProductResponse>>> search(
        ProductSearchRequest searchRequest
    ) {
        ProductSearchQuery searchQuery = searchRequest.toQuery();
        Page<ProductResult> productPage = productQueryUseCase.search(searchQuery);
        Page<ProductResponse> productResponse = productPage.map(ProductResponse::from);

        PageResponse<ProductResponse> pageResponse = PageResponse.of(productResponse);

        return ResponseEntity.ok(CommonResponse.success(pageResponse));
    }
}
