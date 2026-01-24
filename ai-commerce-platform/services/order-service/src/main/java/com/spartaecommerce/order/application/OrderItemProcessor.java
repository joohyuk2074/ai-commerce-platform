package com.spartaecommerce.order.application;

import com.spartaecommerce.order.application.dto.command.OrderItemCreateCommand;
import com.spartaecommerce.order.domain.entity.Category;
import com.spartaecommerce.order.domain.entity.Product;
import com.spartaecommerce.order.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.order.domain.port.out.LoadProductPort;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 주문 항목 처리 도메인 서비스 (MSA 버전) - ecommerce-service의 Product API를 통해 상품 조회 및 재고 관리
 */
@Service
@RequiredArgsConstructor
public class OrderItemProcessor {

  private final LoadProductPort loadProductPort;
  private final LoadCategoryPort loadCategoryPort;

  public List<Product> loadProducts(List<OrderItemCreateCommand> orderItemCreateCommands) {
    List<Long> productIds = orderItemCreateCommands.stream()
        .map(OrderItemCreateCommand::productId)
        .toList();

    return loadProductPort.getProducts(productIds);
  }

  public Map<Long, Product> indexProductsByProductId(List<Product> products) {
    return products.stream()
        .collect(Collectors.toMap(
            Product::productId,
            product -> product
        ));
  }

  public Map<Long, Category> indexCategoriesByProductId(List<Product> products) {
    Set<Long> categoryIds = products.stream()
        .map(Product::categoryId)
        .collect(Collectors.toSet());

    List<Category> categories = loadCategoryPort.getCategories(categoryIds.stream().toList());

    return categories.stream()
        .collect(Collectors.toMap(
            Category::categoryId,
            category -> category
        ));
  }

  public void deductStocks(List<Product> products,
      List<OrderItemCreateCommand> orderItemCreateCommands) {
    Map<Long, Integer> quantities = orderItemCreateCommands.stream()
        .collect(Collectors.toMap(
            OrderItemCreateCommand::productId,
            OrderItemCreateCommand::quantity
        ));

    // Validate stock availability locally first
    for (Product product : products) {
      Integer requestedQuantity = quantities.get(product.productId());

      // TODO: event 방식으로 처리
//      product.deductQuantity(requestedQuantity);  // Validation only
    }

    // TODO: event 방식으로 처리
//    // Call external service to actually deduct stock
//    ecommerceServiceClient.deductStocks(quantities);
  }

  public void restoreStocks(List<Product> products, Map<Long, Integer> productIdToQuantity) {
    // Call external service to restore stock
    // TODO: event 방식으로 처리
//    ecommerceServiceClient.restoreStocks(productIdToQuantity);
  }
}
