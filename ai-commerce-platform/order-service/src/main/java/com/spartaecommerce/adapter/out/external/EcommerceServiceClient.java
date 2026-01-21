package com.spartaecommerce.adapter.out.external;

import com.spartaecommerce.adapter.out.external.dto.*;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.category.Category;
import com.spartaecommerce.common.domain.pointwallet.PointWallet;
import com.spartaecommerce.common.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WebClient based client for ecommerce-service Internal API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcommerceServiceClient {

    private final WebClient ecommerceServiceWebClient;

    /**
     * Get products by IDs
     */
    public List<Product> getProducts(List<Long> productIds) {
        CommonResponse<List<ProductDto>> response = ecommerceServiceWebClient
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

        ecommerceServiceWebClient
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

        ecommerceServiceWebClient
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
        CommonResponse<List<CategoryDto>> response = ecommerceServiceWebClient
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

    /**
     * Get point wallet by user ID
     */
    public PointWallet getPointWallet(Long userId) {
        CommonResponse<PointWalletDto> response = ecommerceServiceWebClient
            .get()
            .uri("/internal/v1/point-wallets/users/{userId}", userId)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<CommonResponse<PointWalletDto>>() {})
            .block();

        if (response == null || response.getData() == null) {
            throw new IllegalStateException("PointWallet not found for user: " + userId);
        }

        return response.getData().toDomain();
    }

    /**
     * Use points
     */
    public void usePoints(Long userId, BigDecimal amount, String description) {
        PointOperationRequest request = new PointOperationRequest(amount, description);

        ecommerceServiceWebClient
            .post()
            .uri("/internal/v1/point-wallets/users/{userId}/use", userId)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Void.class)
            .block();

        log.info("Points used for user {}: {}", userId, amount);
    }

    /**
     * Earn points
     */
    public void earnPoints(Long userId, BigDecimal amount, String description) {
        PointOperationRequest request = new PointOperationRequest(amount, description);

        ecommerceServiceWebClient
            .post()
            .uri("/internal/v1/point-wallets/users/{userId}/earn", userId)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Void.class)
            .block();

        log.info("Points earned for user {}: {}", userId, amount);
    }

    /**
     * Calculate expected points
     */
    public BigDecimal calculatePoints(List<OrderItemForCalculation> orderItems, String userGrade) {
        PointCalculationRequest request = new PointCalculationRequest(
            orderItems.stream()
                .map(item -> new PointCalculationRequest.OrderItemDto(
                    item.productId(),
                    item.categoryId(),
                    item.totalPrice()
                ))
                .toList(),
            userGrade
        );

        CommonResponse<PointCalculationResponse> response = ecommerceServiceWebClient
            .post()
            .uri("/internal/v1/point-wallets/calculate-points")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<CommonResponse<PointCalculationResponse>>() {})
            .block();

        if (response == null || response.getData() == null) {
            return BigDecimal.ZERO;
        }

        return response.getData().expectedPoints();
    }

    public record OrderItemForCalculation(
        Long productId,
        Long categoryId,
        BigDecimal totalPrice
    ) {
    }

    public record PointCalculationRequest(
        List<OrderItemDto> orderItems,
        String userGrade
    ) {
        public record OrderItemDto(
            Long productId,
            Long categoryId,
            BigDecimal totalPrice
        ) {
        }
    }

    public record PointCalculationResponse(
        BigDecimal expectedPoints
    ) {
    }
}
