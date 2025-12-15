package com.spartaecommerce.product.application.service;

import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.domain.port.out.LoadOrderPort;
import com.spartaecommerce.order.domain.repository.OrderRepository;
import com.spartaecommerce.product.application.dto.command.ProductRegisterCommand;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.in.ProductCommandUseCase;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import com.spartaecommerce.product.domain.port.out.SaveProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCommandService implements ProductCommandUseCase {

    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;
    private final LoadCategoryPort loadCategoryPort;
    private final LoadOrderPort loadOrderPort;

    @Override
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

        return saveProductPort.save(product);
    }

    @Override
    public void update(ProductUpdateCommand updateCommand) {
        validateCategoryExists(updateCommand.categoryId());
        validateProductNameUniqueOnUpdate(updateCommand.productId(), updateCommand.name());

        Product product = loadProductPort.getById(updateCommand.productId());

        product.update(
            updateCommand.name(),
            updateCommand.description(),
            updateCommand.price(),
            updateCommand.stock(),
            updateCommand.categoryId()
        );

        saveProductPort.save(product);
    }

    @Override
    public void delete(Long productId) {
        validateNotInCompletedOrders(productId);

        Product product = loadProductPort.getById(productId);
        product.delete();

        saveProductPort.save(product);
    }

    private void validateCategoryExists(Long categoryId) {
        if (!loadCategoryPort.existsById(categoryId)) {
            throw new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Failed to find category. categoryId: " + categoryId
            );
        }
    }

    private void validateProductNameUniqueOnRegister(String productName) {
        if (loadProductPort.existsByName(productName)) {
            throw new BusinessException(
                ErrorCode.ENTITY_ALREADY_EXISTS,
                "Product with the same name already exists: " + productName
            );
        }
    }

    private void validateProductNameUniqueOnUpdate(Long targetProductId, String requestProductName) {
        loadProductPort.findByName(requestProductName)
            .filter(existingProduct -> !existingProduct.getProductId().equals(targetProductId))
            .ifPresent(existingProduct -> {
                throw new BusinessException(
                    ErrorCode.ENTITY_ALREADY_EXISTS,
                    "Cannot update product. Another product with the same name already exists: " + requestProductName
                );
            });
    }

    private void validateNotInCompletedOrders(Long productId) {
        if (loadOrderPort.existsByProductInOrdersWithStatus(productId, OrderStatus.COMPLETED)) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot delete product. It is associated with one or more completed orders. productId: " + productId
            );
        }
    }
}
