package com.spartaecommerce.category.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 계층 구조를 효율적으로 조회하기 위한 Closure Table 엔티티
 *
 * 예시:
 * ancestor_id | descendant_id | depth
 * ------------|---------------|-------
 * 1           | 1             | 0      (자기 자신)
 * 1           | 2             | 1      (1의 직접 자식)
 * 1           | 3             | 2      (1의 손자)
 * 2           | 2             | 0      (자기 자신)
 * 2           | 3             | 1      (2의 직접 자식)
 * 3           | 3             | 0      (자기 자신)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "category_closure", indexes = {
    @Index(name = "idx_descendant", columnList = "descendant_id"),
    @Index(name = "idx_depth", columnList = "depth")
})
public class CategoryClosureJpaEntity {

    @EmbeddedId
    private CategoryClosureId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor_id", insertable = false, updatable = false)
    private CategoryJpaEntity ancestor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendant_id", insertable = false, updatable = false)
    private CategoryJpaEntity descendant;

    @Column(nullable = false)
    private Integer depth;

    public static CategoryClosureJpaEntity of(Long ancestorId, Long descendantId, Integer depth) {
        return new CategoryClosureJpaEntity(
            new CategoryClosureId(ancestorId, descendantId),
            null,
            null,
            depth
        );
    }

    public Long getAncestorId() {
        return id.getAncestorId();
    }

    public Long getDescendantId() {
        return id.getDescendantId();
    }
}
