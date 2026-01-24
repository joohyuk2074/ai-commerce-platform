package com.spartaecommerce.adapter.out.external;

import com.spartaecommerce.adapter.out.external.dto.PointOperationRequest;
import com.spartaecommerce.adapter.out.external.dto.PointWalletDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient based client for ecommerce-service Internal API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcommerceServiceClient {

  private final WebClient ecommerceServiceWebClient;
  private final ProductServiceClient productServiceClient;

  /**
   * Get products by IDs - Delegates to ProductServiceClient
   */
  public List<Product> getProducts(List<Long> productIds) {
    return productServiceClient.getProducts(productIds);
  }

  /**
   * Deduct stock from products - Delegates to ProductServiceClient
   */
  public void deductStocks(Map<Long, Integer> productIdToQuantity) {
    productServiceClient.deductStocks(productIdToQuantity);
  }

  /**
   * Restore stock to products - Delegates to ProductServiceClient
   */
  public void restoreStocks(Map<Long, Integer> productIdToQuantity) {
    productServiceClient.restoreStocks(productIdToQuantity);
  }

  /**
   * Get categories by IDs - Delegates to ProductServiceClient
   */
  public List<Category> getCategories(List<Long> categoryIds) {
    return productServiceClient.getCategories(categoryIds);
  }

  /**
   * Get point wallet by user ID
   */
  public PointWallet getPointWallet(Long userId) {
    CommonResponse<PointWalletDto> response = ecommerceServiceWebClient
        .get()
        .uri("/internal/v1/point-wallets/users/{userId}", userId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<CommonResponse<PointWalletDto>>() {
        })
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
        .bodyToMono(new ParameterizedTypeReference<CommonResponse<PointCalculationResponse>>() {
        })
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
