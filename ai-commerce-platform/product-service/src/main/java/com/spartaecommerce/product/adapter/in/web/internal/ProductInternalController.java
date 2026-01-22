package com.spartaecommerce.product.adapter.in.web.internal;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.product.adapter.in.web.internal.dto.ProductDto;
import com.spartaecommerce.product.adapter.in.web.internal.dto.ProductStockRequest;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.in.ProductQueryUseCase;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import com.spartaecommerce.product.domain.port.out.SaveProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API for inter-service communication
 * This API is only accessible from other services (order-service)
 */
@RestController
@RequestMapping("/internal/v1/products")
@RequiredArgsConstructor
public class ProductInternalController {

    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;

    /**
     * Bulk get products by IDs
     */
    @PostMapping("/bulk")
    public CommonResponse<List<ProductDto>> getProducts(
        @RequestBody List<Long> productIds
    ) {
        List<Product> products = loadProductPort.findAllByProductIdIn(productIds);
        List<ProductDto> productDtos = products.stream()
            .map(ProductDto::from)
            .toList();
        return CommonResponse.success(productDtos);
    }

    /**
     * Deduct stock from products
     */
    @PostMapping("/stocks/deduct")
    public CommonResponse<Void> deductStocks(
        @RequestBody List<ProductStockRequest> requests
    ) {
        List<Long> productIds = requests.stream()
            .map(ProductStockRequest::productId)
            .toList();

        List<Product> products = loadProductPort.findAllByProductIdIn(productIds);

        // Deduct stock for each product
        for (ProductStockRequest request : requests) {
            Product product = products.stream()
                .filter(p -> p.getProductId().equals(request.productId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.productId()));

            product.deductQuantity(request.quantity());
        }

        saveProductPort.saveAll(products);
        return CommonResponse.success(null);
    }

    /**
     * Restore stock to products
     */
    @PostMapping("/stocks/restore")
    public CommonResponse<Void> restoreStocks(
        @RequestBody List<ProductStockRequest> requests
    ) {
        List<Long> productIds = requests.stream()
            .map(ProductStockRequest::productId)
            .toList();

        List<Product> products = loadProductPort.findAllByProductIdIn(productIds);

        // Restore stock for each product
        for (ProductStockRequest request : requests) {
            Product product = products.stream()
                .filter(p -> p.getProductId().equals(request.productId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.productId()));

            product.restoreQuantity(request.quantity());
        }

        saveProductPort.saveAll(products);
        return CommonResponse.success(null);
    }
}
