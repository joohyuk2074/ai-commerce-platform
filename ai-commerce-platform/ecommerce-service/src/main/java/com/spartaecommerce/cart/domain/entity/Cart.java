package com.spartaecommerce.cart.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Cart {

    private Long cartId;

    private Long userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Cart createNew(Long userId) {
        if (userId == null) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "UserId cannot be null"
            );
        }

        return Cart.builder()
            .userId(userId)
            .items(new ArrayList<>())
            .build();
    }

    public void addItem(Long productId, Integer quantity, Money price, Integer maxItems) {
        Optional<CartItem> existingItem = findItemByProductId(productId);

        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
        } else {
            validateMaxItems(maxItems);
            CartItem newItem = CartItem.createNew(cartId, productId, quantity, price);
            items.add(newItem);
        }
    }

    public void removeItem(Long productId) {
        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Product not found in cart: " + productId
            );
        }
    }

    public void updateItemQuantity(Long productId, Integer newQuantity) {
        CartItem item = findItemByProductId(productId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Product not found in cart: " + productId
            ));

        item.updateQuantity(newQuantity);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Money getTotalAmount() {
        return items.stream()
            .map(CartItem::getTotalPrice)
            .reduce(Money.ZERO, Money::add);
    }

    public BigDecimal calculateExpectedPoints(BigDecimal categoryWeight, BigDecimal couponMultiplier) {
        Money totalAmount = getTotalAmount();

        // 기본 포인트 = 총 금액 * 카테고리 가중치
        BigDecimal basePoints = totalAmount.amount().multiply(categoryWeight);

        // 쿠폰 배수 적용
        BigDecimal finalPoints = basePoints.multiply(couponMultiplier);

        return finalPoints.setScale(0, BigDecimal.ROUND_DOWN);  // 소수점 버림
    }

    private Optional<CartItem> findItemByProductId(Long productId) {
        return items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst();
    }

    private void validateMaxItems(Integer maxItems) {
        if (items.size() >= maxItems) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot add more items. Maximum " + maxItems + " items allowed"
            );
        }
    }
}