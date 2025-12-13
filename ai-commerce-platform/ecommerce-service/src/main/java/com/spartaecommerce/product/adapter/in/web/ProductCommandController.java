package com.spartaecommerce.product.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.product.adapter.in.web.dto.request.ProductRegisterRequest;
import com.spartaecommerce.product.adapter.in.web.dto.request.ProductUpdateRequest;
import com.spartaecommerce.product.application.dto.command.ProductRegisterCommand;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;
import com.spartaecommerce.product.domain.port.in.ProductCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductCommandController {

    private final ProductCommandUseCase productCommandUseCase;

    @PostMapping("/products")
    public ResponseEntity<CommonResponse<IdResponse>> create(
        @Valid @RequestBody ProductRegisterRequest registerRequest
    ) {
        ProductRegisterCommand registerCommand = registerRequest.toCommand();

        Long productId = productCommandUseCase.register(registerCommand);

        return ResponseEntity
            .created(URI.create("/api/v1/products/" + productId))
            .body(CommonResponse.create(productId));
    }

    @PatchMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<Void>> updateProduct(
        @PathVariable Long productId,
        @Valid @RequestBody ProductUpdateRequest updateRequest
    ) {
        ProductUpdateCommand updateCommand = updateRequest.toCommand(productId);
        productCommandUseCase.update(updateCommand);

        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productCommandUseCase.delete(productId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
