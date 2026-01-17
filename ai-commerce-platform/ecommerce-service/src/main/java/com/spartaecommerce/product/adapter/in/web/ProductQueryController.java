package com.spartaecommerce.product.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.product.adapter.in.web.dto.request.ProductSearchRequest;
import com.spartaecommerce.product.adapter.in.web.dto.response.ProductResponse;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.application.dto.result.ProductResult;
import com.spartaecommerce.product.domain.port.in.ProductQueryUseCase;
import com.spartaecommerce.util.PageResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product Query", description = "상품 조회 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductQueryUseCase productQueryUseCase;

    @Operation(summary = "상품 단건 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없습니다."
        )
    })
    @GetMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> getProduct(
        @Parameter(description = "조회할 상품 ID", required = true, example = "1")
        @PathVariable Long productId
    ) {
        ProductResult productResult = productQueryUseCase.getProduct(productId);
        ProductResponse productResponse = ProductResponse.from(productResult);

        CommonResponse<ProductResponse> commonResponse = CommonResponse.success(productResponse);
        return ResponseEntity.ok(commonResponse);
    }

    @Operation(summary = "상품 검색", description = "다양한 조건으로 상품을 검색합니다. 카테고리, 가격 범위, 재고 상태 등으로 필터링할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "검색 성공",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
        )
    })
    @GetMapping("/products")
    public ResponseEntity<CommonResponse<PageResponse<ProductResponse>>> search(
        @Parameter(description = "상품 검색 조건")
        ProductSearchRequest searchRequest
    ) {
        ProductSearchQuery searchQuery = searchRequest.toQuery();
        Page<ProductResult> productPage = productQueryUseCase.search(searchQuery);
        Page<ProductResponse> productResponse = productPage.map(ProductResponse::from);

        PageResponse<ProductResponse> pageResponse = PageResponseMapper.of(productResponse);

        return ResponseEntity.ok(CommonResponse.success(pageResponse));
    }
}
