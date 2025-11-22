package com.spartaecommerce.order.application;

import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.repository.CategoryRepository;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.application.dto.command.OrderItemCreateCommand;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 주문 항목 처리 도메인 서비스
 * - 상품 조회 및 검증
 * - 재고 차감 및 복구
 */
@Service
@RequiredArgsConstructor
public class OrderItemProcessor {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<Product> loadProducts(List<OrderItemCreateCommand> orderItemCreateCommands) {
        List<Long> productIds = orderItemCreateCommands.stream()
            .map(OrderItemCreateCommand::productId)
            .toList();

        List<Product> products = productRepository.findAllByProductIdIn(productIds);
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

        return categoryRepository
            .findAllByCategoryIdIn(categoryIds)
            .stream()
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

        for (Product product : products) {
            Integer requestedQuantity = quantities.get(product.getProductId());
            product.deductQuantity(requestedQuantity);
        }
    }

    public void restoreStocks(List<Product> products, Map<Long, Integer> productIdToQuantity) {
        for (Product product : products) {
            Integer quantity = productIdToQuantity.get(product.getProductId());
            if (quantity != null) {
                product.restoreQuantity(quantity);
            }
        }
    }

    public void saveProducts(List<Product> products) {
        productRepository.saveAll(products);
    }
}