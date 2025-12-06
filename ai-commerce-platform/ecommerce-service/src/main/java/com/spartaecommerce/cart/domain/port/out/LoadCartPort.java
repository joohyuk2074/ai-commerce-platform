package com.spartaecommerce.cart.domain.port.out;

import com.spartaecommerce.cart.domain.entity.Cart;

import java.util.Optional;

public interface LoadCartPort {

    Optional<Cart> findByUserId(Long userId);

    Cart getByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
