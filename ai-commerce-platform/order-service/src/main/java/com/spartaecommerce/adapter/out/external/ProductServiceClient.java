package com.spartaecommerce.adapter.out.external;

import com.spartaecommerce.adapter.out.external.dto.CategoryDto;
import com.spartaecommerce.adapter.out.external.dto.ProductDto;
import com.spartaecommerce.adapter.out.external.dto.ProductStockRequest;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.category.Category;
import com.spartaecommerce.common.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * WebClient based client for product-service Internal API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final WebClient productServiceWebClient;

    /**
     * Get products by IDs
     */
    public List<Product> getProducts(List<Long> productIds) {
        CommonResponse<List<ProductDto>> response = productServiceWebClient
            .post()
            .uri("/internal/v1/products/bulk")
            .bodyValue(productIds)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<CommonResponse<List<ProductDto>>>() {})
            .block();

        if (response == null || response.getData() == null) {
            return List.of();
        }

        return response.getData().stream()
            .map(ProductDto::toDomain)
            .toList();
    }

    /**
     * Deduct stock from products
     */
    public void deductStocks(Map<Long, Integer> productIdToQuantity) {
        List<ProductStockRequest> requests = productIdToQuantity.entrySet().stream()
            .map(entry -> new ProductStockRequest(entry.getKey(), entry.getValue()))
            .toList();

        productServiceWebClient
            .post()
            .uri("/internal/v1/products/stocks/deduct")
            .bodyValue(requests)
            .retrieve()
            .bodyToMono(Void.class)
            .block();

        log.info("Stock deducted for products: {}", productIdToQuantity);
    }

    /**
     * Restore stock to products
     */
    public void restoreStocks(Map<Long, Integer> productIdToQuantity) {
        List<ProductStockRequest> requests = productIdToQuantity.entrySet().stream()
            .map(entry -> new ProductStockRequest(entry.getKey(), entry.getValue()))
            .toList();

        productServiceWebClient
            .post()
            .uri("/internal/v1/products/stocks/restore")
            .bodyValue(requests)
            .retrieve()
            .bodyToMono(Void.class)
            .block();

        log.info("Stock restored for products: {}", productIdToQuantity);
    }

    /**
     * Get categories by IDs
     */
    public List<Category> getCategories(List<Long> categoryIds) {
        CommonResponse<List<CategoryDto>> response = productServiceWebClient
            .post()
            .uri("/internal/v1/categories/bulk")
            .bodyValue(categoryIds)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<CommonResponse<List<CategoryDto>>>() {})
            .block();

        if (response == null || response.getData() == null) {
            return List.of();
        }

        return response.getData().stream()
            .map(CategoryDto::toDomain)
            .toList();
    }
}
