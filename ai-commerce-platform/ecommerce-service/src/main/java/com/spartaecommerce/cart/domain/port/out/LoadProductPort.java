package com.spartaecommerce.cart.domain.port.out;

import com.spartaecommerce.product.domain.entity.Product;

import java.util.List;

public interface LoadProductPort {

    Product getById(Long productId);

    List<Product> findAllByProductIdIn(List<Long> productIds);
}
