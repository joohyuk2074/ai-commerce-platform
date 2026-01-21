package com.spartaecommerce.product.domain.port.out;

import com.spartaecommerce.common.domain.product.Product;

import java.util.List;

public interface SaveProductPort {

    Long save(Product product);

    void saveAll(List<Product> products);
}
