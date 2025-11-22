package com.spartaecommerce.cart.infrastructure.persistence.jpa.entity;

import com.spartaecommerce.cart.domain.entity.CartItem;
import com.spartaecommerce.common.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "cart_items")
public class CartItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @Column(nullable = false, insertable = false, updatable = false)
    private Long cartId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static CartItemJpaEntity from(CartItem cartItem) {
        return new CartItemJpaEntity(
            cartItem.getCartItemId(),
            cartItem.getCartId(),
            cartItem.getProductId(),
            cartItem.getQuantity(),
            cartItem.getPrice().amount(),
            cartItem.getCreatedAt(),
            cartItem.getUpdatedAt()
        );
    }

    public CartItem toDomain() {
        return CartItem.builder()
            .cartItemId(this.cartItemId)
            .cartId(this.cartId)
            .productId(this.productId)
            .quantity(this.quantity)
            .price(Money.from(this.price))
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}