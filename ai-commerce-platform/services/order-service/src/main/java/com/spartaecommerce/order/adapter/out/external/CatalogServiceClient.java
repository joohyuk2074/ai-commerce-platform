package com.spartaecommerce.order.adapter.out.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.spartaecommerce.exception.BusinessException;
import com.spartaecommerce.order.adapter.out.external.dto.ProductResponse;
import com.spartaecommerce.order.domain.entity.Product;
import com.spartaecommerce.order.domain.port.out.LoadProductPort;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogServiceClient implements LoadProductPort {

  private final RestClient catalogServiceRestClient;
  private final CommonResponseParser responseParser;

  /**
   * 상품 단건 조회
   *
   * @param productId 조회할 상품 ID
   * @return 상품 정보
   * @throws BusinessException 상품을 찾을 수 없거나 API 호출 실패 시
   */
  public Product getProduct(Long productId) {
    log.debug("Fetching product from catalog service: productId={}", productId);

    String response = catalogServiceRestClient.get()
        .uri("/api/v1/products/{productId}", productId)
        .retrieve()
        .body(String.class);

    ProductResponse productResponse = responseParser.parse(
        response,
        new TypeReference<>() {
        }
    );

    log.debug("Successfully fetched product: productId={}, name={}",
        productId, productResponse.name());

    return productResponse.toDomain();

  }

  /**
   * 상품 복수 조회
   *
   * @param productIds 조회할 상품 ID 목록
   * @return 상품 정보 목록
   * @throws BusinessException API 호출 실패 시
   */
  @Override
  public List<Product> getProducts(Collection<Long> productIds) {
    log.debug("Fetching products from catalog service: productIds={}", productIds);

    if (productIds == null || productIds.isEmpty()) {
      log.debug("Empty product IDs, returning empty list");
      return List.of();
    }

    String response = catalogServiceRestClient.post()
        .uri("/api/v1/products/batch")
        .body(productIds)
        .retrieve()
        .body(String.class);

    List<ProductResponse> productResponses = responseParser.parse(
        response,
        new TypeReference<>() {
        }
    );

    log.debug("Successfully fetched {} products", productResponses.size());

    return productResponses.stream()
        .map(ProductResponse::toDomain)
        .toList();
  }
}
