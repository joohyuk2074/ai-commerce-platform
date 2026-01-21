package com.spartaecommerce.order.application;

import com.spartaecommerce.adapter.out.external.EcommerceServiceClient;
import com.spartaecommerce.common.domain.category.Category;
import com.spartaecommerce.common.domain.product.Product;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.application.dto.command.OrderItemCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 주문 항목 처리 도메인 서비스 (MSA 버전)
 * - ecommerce-service의 Product API를 통해 상품 조회 및 재고 관리
 */
@Service
@RequiredArgsConstructor
public class OrderItemProcessor {

    private final EcommerceServiceClient ecommerceServiceClient;

    public List<Product> loadProducts(List<OrderItemCreateCommand> orderItemCreateCommands) {
        List<Long> productIds = orderItemCreateCommands.stream()
            .map(OrderItemCreateCommand::productId)
            .toList();

        List<Product> products = ecommerceServiceClient.getProducts(productIds);
        if (products.size() != productIds.size()) {
            throw new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Some products not found"
            );
        }

        return products;
    }

    public Map<Long, Product> indexProductsByProductId(List<Product> products) {
        return products.stream()
            .collect(Collectors.toMap(
                Product::getProductId,
                product -> product
            ));
    }

    public Map<Long, Category> indexCategoriesByProductId(List<Product> products) {
        Set<Long> categoryIds = products.stream()
            .map(Product::getCategoryId)
            .collect(Collectors.toSet());

        List<Category> categories = ecommerceServiceClient.getCategories(categoryIds.stream().toList());

        return categories.stream()
            .collect(Collectors.toMap(
                Category::getCategoryId,
                category -> category
            ));
    }

    public void deductStocks(List<Product> products, List<OrderItemCreateCommand> orderItemCreateCommands) {
        Map<Long, Integer> quantities = orderItemCreateCommands.stream()
            .collect(Collectors.toMap(
                OrderItemCreateCommand::productId,
                OrderItemCreateCommand::quantity
            ));

        // Validate stock availability locally first
        for (Product product : products) {
            Integer requestedQuantity = quantities.get(product.getProductId());
            product.deductQuantity(requestedQuantity);  // Validation only
        }

        // Call external service to actually deduct stock
        ecommerceServiceClient.deductStocks(quantities);
    }

    public void restoreStocks(List<Product> products, Map<Long, Integer> productIdToQuantity) {
        // Call external service to restore stock
        ecommerceServiceClient.restoreStocks(productIdToQuantity);
    }
}
