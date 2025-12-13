package com.spartaecommerce.product.adapter.out.persistence.jpa.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.domain.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "product",
    indexes = {
        @Index(name = "idx_product_category_price_id", columnList = "categoryId, price, productId")
    }
)
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Long categoryId;

    @Column
    private boolean deleted;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static ProductJpaEntity from(Product product) {
        return new ProductJpaEntity(
            product.getProductId(),
            product.getName(),
            product.getDescription(),
            product.getPrice().amount(),
            product.getStock(),
            product.getCategoryId(),
            product.isDeleted(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    public Product toDomain() {
        return Product.builder()
            .productId(this.productId)
            .name(this.name)
            .description(this.description)
            .price(Money.from(this.price))
            .stock(this.stock)
            .categoryId(this.categoryId)
            .deleted(this.deleted)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}