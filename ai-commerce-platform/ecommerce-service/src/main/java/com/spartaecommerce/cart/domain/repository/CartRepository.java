package com.spartaecommerce.cart.domain.repository;

import com.spartaecommerce.cart.domain.entity.Cart;

import java.util.Optional;

public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findByUserId(Long userId);

    Cart getByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);
}