package com.spartaecommerce.cart.domain.storage;

import com.spartaecommerce.cart.domain.entity.Cart;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CartFakeStorage implements CartStorage {

    private final Map<Long, Cart> carts = new ConcurrentHashMap<>();

    @Override
    public void save(Cart cart) {
        carts.put(cart.getCartId(), cart);
    }

    @Override
    public Optional<Cart> get(Long userId) {
        Cart cart = carts.get(userId);

        if (cart == null) {
            return Optional.empty();
        }

        return Optional.of(cart);
    }

    @Override
    public void delete(Long userId) {
        carts.remove(userId);
    }

    public void clear() {
        carts.clear();
    }
}