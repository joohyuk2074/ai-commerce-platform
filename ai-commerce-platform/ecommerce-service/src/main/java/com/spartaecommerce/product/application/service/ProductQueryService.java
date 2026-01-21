package com.spartaecommerce.product.application.service;

import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.application.dto.result.ProductResult;
import com.spartaecommerce.common.domain.product.Product;
import com.spartaecommerce.product.domain.port.in.ProductQueryUseCase;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService implements ProductQueryUseCase {

    private final LoadProductPort loadProductPort;

    @Override
    public ProductResult getProduct(Long productId) {
        Product product = loadProductPort.getById(productId);
        return ProductResult.from(product);
    }

    @Override
    public Page<ProductResult> search(ProductSearchQuery searchQuery) {
        return loadProductPort.search(searchQuery)
            .map(ProductResult::from);
    }
}
