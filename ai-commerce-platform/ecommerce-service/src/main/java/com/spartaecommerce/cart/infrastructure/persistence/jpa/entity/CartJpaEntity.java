package com.spartaecommerce.cart.infrastructure.persistence.jpa.entity;

import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.entity.CartItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "carts")
public class CartJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @Column(nullable = false, unique = true)
    private Long userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "cartId", referencedColumnName = "cartId")
    private List<CartItemJpaEntity> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static CartJpaEntity from(Cart cart) {
        CartJpaEntity cartJpaEntity = new CartJpaEntity(
            cart.getCartId(),
            cart.getUserId(),
            new ArrayList<>(),
            cart.getCreatedAt(),
            cart.getUpdatedAt()
        );

        List<CartItemJpaEntity> itemEntities = cart.getItems().stream()
            .map(CartItemJpaEntity::from)
            .toList();

        cartJpaEntity.items.addAll(itemEntities);

        return cartJpaEntity;
    }

    public Cart toDomain() {
        List<CartItem> domainItems = this.items.stream()
            .map(CartItemJpaEntity::toDomain)
            .toList();

        return Cart.builder()
            .cartId(this.cartId)
            .userId(this.userId)
            .items(new ArrayList<>(domainItems))
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}