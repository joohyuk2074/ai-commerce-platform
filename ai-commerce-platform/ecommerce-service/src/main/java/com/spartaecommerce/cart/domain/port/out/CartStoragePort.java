package com.spartaecommerce.cart.domain.port.out;

import com.spartaecommerce.cart.domain.entity.Cart;

import java.util.Optional;

public interface CartStoragePort {

    void save(Cart cart);

    Optional<Cart> get(Long userId);

    void delete(Long userId);
}
