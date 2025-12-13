package com.spartaecommerce.category.adapter.out.persistence.jpa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.spartaecommerce.category.domain.entity.Category;
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

@Getter
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "category")
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryJpaEntity parent;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted = false;

    public static CategoryJpaEntity from(Category category) {
        return new CategoryJpaEntity(
            category.getCategoryId(),
            category.getName(),
            category.getDescription(),
            null,
            category.getCreatedAt(),
            category.getUpdatedAt(),
            category.isDeleted()
        );
    }

    public Category toDomain() {
        return Category.builder()
            .categoryId(this.categoryId)
            .name(this.name)
            .description(this.description)
            .parentCategoryId(this.parent != null ? this.parent.getCategoryId() : null)
            .childrenCategoryIds(new ArrayList<>())
            .deleted(this.deleted)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}
