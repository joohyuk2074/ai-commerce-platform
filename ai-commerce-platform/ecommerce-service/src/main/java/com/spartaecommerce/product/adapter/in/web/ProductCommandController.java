package com.spartaecommerce.product.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.product.adapter.in.web.dto.request.ProductRegisterRequest;
import com.spartaecommerce.product.adapter.in.web.dto.request.ProductUpdateRequest;
import com.spartaecommerce.product.application.dto.command.ProductRegisterCommand;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;
import com.spartaecommerce.product.domain.port.in.ProductCommandUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Product Command", description = "상품 등록/수정/삭제 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductCommandController {

    private final ProductCommandUseCase productCommandUseCase;

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "상품 등록 성공",
            content = @Content(schema = @Schema(implementation = IdResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검증 실패)"
        )
    })
    @PostMapping("/products")
    public ResponseEntity<CommonResponse<IdResponse>> create(
        @Parameter(description = "상품 등록 정보", required = true)
        @Valid @RequestBody ProductRegisterRequest registerRequest
    ) {
        ProductRegisterCommand registerCommand = registerRequest.toCommand();

        Long productId = productCommandUseCase.register(registerCommand);

        return ResponseEntity
            .created(URI.create("/api/v1/products/" + productId))
            .body(CommonResponse.create(productId));
    }

    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상품 수정 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없습니다."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검증 실패)"
        )
    })
    @PatchMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<Void>> updateProduct(
        @Parameter(description = "수정할 상품 ID", required = true, example = "1")
        @PathVariable Long productId,
        @Parameter(description = "상품 수정 정보", required = true)
        @Valid @RequestBody ProductUpdateRequest updateRequest
    ) {
        ProductUpdateCommand updateCommand = updateRequest.toCommand(productId);
        productCommandUseCase.update(updateCommand);

        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상품 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없습니다."
        )
    })
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<Void>> deleteProduct(
        @Parameter(description = "삭제할 상품 ID", required = true, example = "1")
        @PathVariable Long productId
    ) {
        productCommandUseCase.delete(productId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
