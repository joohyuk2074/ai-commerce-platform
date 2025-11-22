package com.spartaecommerce.cart.domain.repository;

import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CartFakeRepository implements CartRepository {

    private final Map<Long, Cart> repository = new ConcurrentHashMap<>();
    private final Map<Long, Long> userIdToCartIdMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Cart save(Cart cart) {
        if (cart.getCartId() == null) {
            long cartId = idGenerator.getAndIncrement();
            Cart newCart = Cart.builder()
                .cartId(cartId)
                .userId(cart.getUserId())
                .items(new ArrayList<>(cart.getItems()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(cartId, newCart);
            userIdToCartIdMap.put(cart.getUserId(), cartId);
            return newCart;
        } else {
            Cart updatedCart = Cart.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .items(new ArrayList<>(cart.getItems()))
                .createdAt(cart.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(cart.getCartId(), updatedCart);
            return updatedCart;
        }
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        Long cartId = userIdToCartIdMap.get(userId);
        if (cartId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(repository.get(cartId));
    }

    @Override
    public Cart getByUserId(Long userId) {
        return findByUserId(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Cart not found for userId: " + userId
            ));
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return userIdToCartIdMap.containsKey(userId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        Long cartId = userIdToCartIdMap.get(userId);
        if (cartId != null) {
            repository.remove(cartId);
            userIdToCartIdMap.remove(userId);
        }
    }

    public void clear() {
        repository.clear();
        userIdToCartIdMap.clear();
    }
}