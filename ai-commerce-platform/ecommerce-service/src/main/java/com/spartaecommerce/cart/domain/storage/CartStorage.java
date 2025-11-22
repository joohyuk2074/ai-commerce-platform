package com.spartaecommerce.cart.domain.storage;

import com.spartaecommerce.cart.domain.entity.Cart;

import java.util.Optional;

public interface CartStorage {

    void save(Cart cart);

    Optional<Cart> get(Long userId);

    void delete(Long userId);
}
