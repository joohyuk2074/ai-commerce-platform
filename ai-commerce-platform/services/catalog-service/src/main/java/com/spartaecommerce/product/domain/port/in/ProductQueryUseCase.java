package com.spartaecommerce.product.domain.port.in;

import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.application.dto.result.ProductResult;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductQueryUseCase {

    ProductResult getProduct(Long productId);

    List<ProductResult> getProducts(List<Long> productIds);

    Page<ProductResult> search(ProductSearchQuery searchQuery);
}
