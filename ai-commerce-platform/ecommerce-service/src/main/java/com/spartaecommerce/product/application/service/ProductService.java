package com.spartaecommerce.product.application.service;

import com.spartaecommerce.category.domain.repository.CategoryRepository;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.domain.repository.OrderRepository;
import com.spartaecommerce.product.application.dto.result.ProductResult;
import com.spartaecommerce.product.application.dto.command.ProductRegisterCommand;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Long register(ProductRegisterCommand registerCommand) {
        validateCategoryExists(registerCommand.categoryId());
        validateProductNameUniqueOnRegister(registerCommand.name());

        Product product = Product.createNew(
            registerCommand.name(),
            registerCommand.description(),
            registerCommand.price(),
            registerCommand.stock(),
            registerCommand.categoryId()
        );

        return productRepository.save(product);
    }

    @Transactional
    public void update(Long productId, ProductUpdateCommand updateCommand) {
        validateCategoryExists(updateCommand.categoryId());
        validateProductNameUniqueOnUpdate(productId, updateCommand.name());

        Product product = productRepository.getById(productId);

        product.update(
            updateCommand.name(),
            updateCommand.description(),
            updateCommand.price(),
            updateCommand.stock(),
            updateCommand.categoryId()
        );

        productRepository.save(product);
    }

    public ProductResult getProduct(Long productId) {
        Product product = productRepository.getById(productId);
        return ProductResult.from(product);
    }

    public Page<ProductResult> search(ProductSearchQuery searchQuery) {
        return productRepository.search(searchQuery)
            .map(ProductResult::from);
    }

    @Transactional
    public void delete(Long productId) {
        validateNotInCompletedOrders(productId);

        Product product = productRepository.getById(productId);
        product.delete();

        productRepository.save(product);
    }

    private void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Failed to find category. categoryId: " + categoryId
            );
        }
    }

    private void validateProductNameUniqueOnRegister(String productName) {
        if (productRepository.existsByName(productName)) {
            throw new BusinessException(
                ErrorCode.ENTITY_ALREADY_EXISTS,
                "Product with the same name already exists: " + productName
            );
        }
    }

    private void validateProductNameUniqueOnUpdate(Long targetProductId, String requestProductName) {
        productRepository.findByName(requestProductName)
            .filter(existingProduct -> !existingProduct.getProductId().equals(targetProductId))
            .ifPresent(existingProduct -> {
                throw new BusinessException(
                    ErrorCode.ENTITY_ALREADY_EXISTS,
                    "Cannot update product. Another product with the same name already exists: " + requestProductName
                );
            });
    }

    private void validateNotInCompletedOrders(Long productId) {
        if (orderRepository.existsByProductInOrdersWithStatus(productId, OrderStatus.COMPLETED)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot delete product. It is associated with one or more completed orders. productId: " + productId
            );
        }
    }
}
