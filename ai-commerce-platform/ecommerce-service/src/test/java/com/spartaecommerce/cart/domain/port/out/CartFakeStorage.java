package com.spartaecommerce.cart.domain.port.out;

import com.spartaecommerce.cart.domain.entity.Cart;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CartFakeStorage implements CartStoragePort {

    private final Map<Long, Cart> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Cart cart) {
        if (cart != null && cart.getUserId() != null) {
            storage.put(cart.getUserId(), cart);
        }
    }

    @Override
    public Optional<Cart> get(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(userId));
    }

    @Override
    public void delete(Long userId) {
        if (userId != null) {
            storage.remove(userId);
        }
    }

    public void clear() {
        storage.clear();
    }
}
