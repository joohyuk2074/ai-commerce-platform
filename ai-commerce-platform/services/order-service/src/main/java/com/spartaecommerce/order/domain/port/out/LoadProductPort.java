package com.spartaecommerce.order.domain.port.out;

import com.spartaecommerce.order.domain.entity.Product;
import java.util.Collection;
import java.util.List;

public interface LoadProductPort {

  Product getProduct(Long productId);

  List<Product> getProducts(Collection<Long> productIds);
}
