package com.spartaecommerce.product.domain.service;

import com.spartaecommerce.common.infrastructure.lock.DistributedLock;
import com.spartaecommerce.common.infrastructure.lock.manager.DistributedLockManager;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;

import java.util.List;

public class ProductStockService {

    private final ProductRepository productRepository;
    private final DistributedLockManager distributedLockManager;

    public ProductStockService(
        ProductRepository productRepository,
        DistributedLockManager distributedLockManager
    ) {
        this.productRepository = productRepository;
        this.distributedLockManager = distributedLockManager;
    }

    @DistributedLock(
        key = "'product:' + #productId + ':stock'",
        errorMessage = "재고 처리 중입니다. 잠시 후 다시 시도해주세요."
    )
    public void deduct(Long productId, Integer quantity) {
        Product product = productRepository.getById(productId);
        product.deductQuantity(quantity);
        productRepository.save(product);
    }
}