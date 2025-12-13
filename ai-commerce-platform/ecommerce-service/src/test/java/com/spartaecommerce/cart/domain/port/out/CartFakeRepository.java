package com.spartaecommerce.cart.domain.port.out;

import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.entity.CartItem;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CartFakeRepository implements LoadCartPort, SaveCartPort {

    private final Map<Long, Cart> cartByIdMap = new ConcurrentHashMap<>();
    private final Map<Long, Cart> cartByUserIdMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Cart save(Cart cart) {
        if (cart.getCartId() == null) {
            // 새로운 장바구니 저장
            long cartId = idGenerator.getAndIncrement();
            Cart newCart = Cart.builder()
                .cartId(cartId)
                .userId(cart.getUserId())
                .items(cloneItems(cart.getItems(), cartId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            cartByIdMap.put(cartId, newCart);
            cartByUserIdMap.put(newCart.getUserId(), newCart);
            return newCart;
        } else {
            // 기존 장바구니 업데이트
            Cart updatedCart = Cart.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .items(cloneItems(cart.getItems(), cart.getCartId()))
                .createdAt(cart.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

            cartByIdMap.put(updatedCart.getCartId(), updatedCart);
            cartByUserIdMap.put(updatedCart.getUserId(), updatedCart);
            return updatedCart;
        }
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        return Optional.ofNullable(cartByUserIdMap.get(userId));
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
        return cartByUserIdMap.containsKey(userId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        Cart cart = cartByUserIdMap.remove(userId);
        if (cart != null) {
            cartByIdMap.remove(cart.getCartId());
        }
    }

    private ArrayList<CartItem> cloneItems(java.util.List<CartItem> items, Long cartId) {
        AtomicLong itemIdGenerator = new AtomicLong(1L);
        return items.stream()
            .map(item -> CartItem.builder()
                .cartItemId(item.getCartItemId() != null ? item.getCartItemId() : itemIdGenerator.getAndIncrement())
                .cartId(cartId)
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .createdAt(item.getCreatedAt() != null ? item.getCreatedAt() : LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build())
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void clear() {
        cartByIdMap.clear();
        cartByUserIdMap.clear();
        idGenerator.set(1L);
    }
}
