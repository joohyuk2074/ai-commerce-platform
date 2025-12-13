package com.spartaecommerce.product.domain.port.in;

import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.application.dto.result.ProductResult;
import org.springframework.data.domain.Page;

public interface ProductQueryUseCase {

    ProductResult getProduct(Long productId);

    Page<ProductResult> search(ProductSearchQuery searchQuery);
}
