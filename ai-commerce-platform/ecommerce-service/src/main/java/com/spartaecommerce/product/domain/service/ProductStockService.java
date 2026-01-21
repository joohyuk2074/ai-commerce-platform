package com.spartaecommerce.product.domain.service;

import com.spartaecommerce.common.infrastructure.lock.DistributedLock;
import com.spartaecommerce.common.infrastructure.lock.manager.DistributedLockManager;
import com.spartaecommerce.common.domain.product.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import com.spartaecommerce.product.domain.port.out.SaveProductPort;

public class ProductStockService {

    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;
    private final DistributedLockManager distributedLockManager;

    public ProductStockService(
        LoadProductPort loadProductPort,
        SaveProductPort saveProductPort,
        DistributedLockManager distributedLockManager
    ) {
        this.loadProductPort = loadProductPort;
        this.saveProductPort = saveProductPort;
        this.distributedLockManager = distributedLockManager;
    }

    @DistributedLock(
        key = "'product:' + #productId + ':stock'",
        errorMessage = "재고 처리 중입니다. 잠시 후 다시 시도해주세요."
    )
    public void deduct(Long productId, Integer quantity) {
        Product product = loadProductPort.getById(productId);
        product.deductQuantity(quantity);
        saveProductPort.save(product);
    }
}