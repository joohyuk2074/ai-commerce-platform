package com.spartaecommerce.product.presentation.controller;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.product.application.dto.result.ProductResult;
import com.spartaecommerce.product.application.service.ProductService;
import com.spartaecommerce.product.application.dto.command.ProductRegisterCommand;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.presentation.controller.dto.request.ProductRegisterRequest;
import com.spartaecommerce.product.presentation.controller.dto.request.ProductSearchRequest;
import com.spartaecommerce.product.presentation.controller.dto.request.ProductUpdateRequest;
import com.spartaecommerce.product.presentation.controller.dto.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/products")
    public ResponseEntity<CommonResponse<IdResponse>> create(
        @Valid @RequestBody ProductRegisterRequest registerRequest
    ) {
        ProductRegisterCommand registerCommand = registerRequest.toCommand();

        Long productId = productService.register(registerCommand);

        return ResponseEntity
            .created(URI.create("/api/v1/products/" + productId))
            .body(CommonResponse.create(productId));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> getProduct(
        @PathVariable Long productId
    ) {
        ProductResult productResult = productService.getProduct(productId);
        ProductResponse productResponse = ProductResponse.from(productResult);

        CommonResponse<ProductResponse> commonResponse = CommonResponse.success(productResponse);
        return ResponseEntity.ok(commonResponse);
    }

    @GetMapping("/products")
    public ResponseEntity<CommonResponse<PageResponse<ProductResponse>>> search(
        ProductSearchRequest searchRequest
    ) {
        ProductSearchQuery searchQuery = searchRequest.toQuery();
        Page<ProductResult> productPage = productService.search(searchQuery);
        Page<ProductResponse> productResponse = productPage.map(ProductResponse::from);

        PageResponse<ProductResponse> pageResponse = PageResponse.of(productResponse);

        return ResponseEntity.ok(CommonResponse.success(pageResponse));
    }

    @PatchMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<Void>> updateProduct(
        @PathVariable Long productId,
        @Valid @RequestBody ProductUpdateRequest updateRequest
    ) {
        ProductUpdateCommand updateCommand = updateRequest.toCommand();
        productService.update(productId, updateCommand);

        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.delete(productId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
