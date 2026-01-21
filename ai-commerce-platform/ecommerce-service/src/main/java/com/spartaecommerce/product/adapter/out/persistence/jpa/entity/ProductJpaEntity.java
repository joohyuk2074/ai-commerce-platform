package com.spartaecommerce.product.adapter.out.persistence.jpa.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.domain.product.ExternalProductRef;
import com.spartaecommerce.common.domain.product.Product;
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
        @Index(name = "idx_product_search", columnList = "deleted, categoryId, productId")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_vendor_external_id", columnNames = {"vendor", "externalId"})
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

    @Column(length = 100)
    private String vendor;

    @Column(length = 100)
    private String externalId;

    @Column(nullable = false)
    private boolean isOrderable;

    @Column
    private boolean deleted;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static ProductJpaEntity from(Product product) {
        String vendor = null;
        String externalId = null;

        if (product.getExternalProductRef() != null) {
            vendor = product.getExternalProductRef().vendor();
            externalId = product.getExternalProductRef().externalId();
        }

        return new ProductJpaEntity(
            product.getProductId(),
            product.getName(),
            product.getDescription(),
            product.getPrice().amount(),
            product.getStock(),
            product.getCategoryId(),
            vendor,
            externalId,
            product.isOrderable(),
            product.isDeleted(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    public Product toDomain() {
        ExternalProductRef externalProductRef = null;
        if (this.vendor != null && this.externalId != null) {
            externalProductRef = new ExternalProductRef(this.vendor, this.externalId);
        }

        return Product.builder()
            .productId(this.productId)
            .name(this.name)
            .description(this.description)
            .price(Money.from(this.price))
            .stock(this.stock)
            .categoryId(this.categoryId)
            .externalProductRef(externalProductRef)
            .isOrderable(this.isOrderable)
            .deleted(this.deleted)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}