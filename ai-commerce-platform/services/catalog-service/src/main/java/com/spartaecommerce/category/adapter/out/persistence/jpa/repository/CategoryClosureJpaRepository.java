package com.spartaecommerce.category.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.category.adapter.out.persistence.jpa.entity.CategoryClosureId;
import com.spartaecommerce.category.adapter.out.persistence.jpa.entity.CategoryClosureJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryClosureJpaRepository extends JpaRepository<CategoryClosureJpaEntity, CategoryClosureId> {

    /**
     * 특정 조상의 모든 자손 조회 (자기 자신 포함)
     */
    @Query("SELECT cc FROM CategoryClosureJpaEntity cc WHERE cc.id.ancestorId = :ancestorId ORDER BY cc.depth")
    List<CategoryClosureJpaEntity> findAllByAncestorId(@Param("ancestorId") Long ancestorId);

    /**
     * 특정 자손의 모든 조상 조회 (자기 자신 포함)
     */
    @Query("SELECT cc FROM CategoryClosureJpaEntity cc WHERE cc.id.descendantId = :descendantId ORDER BY cc.depth")
    List<CategoryClosureJpaEntity> findAllByDescendantId(@Param("descendantId") Long descendantId);

    /**
     * 최상위 카테고리들의 모든 자손 조회 (전체 트리)
     */
    @Query("""
        SELECT cc FROM CategoryClosureJpaEntity cc
        WHERE cc.id.ancestorId IN :rootCategoryIds
        ORDER BY cc.depth, cc.id.descendantId
        """)
    List<CategoryClosureJpaEntity> findAllByAncestorIdIn(@Param("rootCategoryIds") List<Long> rootCategoryIds);

    /**
     * 특정 자손 노드 삭제 (카테고리 삭제 시)
     */
    @Modifying
    @Query("DELETE FROM CategoryClosureJpaEntity cc WHERE cc.id.descendantId = :descendantId")
    void deleteAllByDescendantId(@Param("descendantId") Long descendantId);

    /**
     * 특정 조상 노드 삭제 (카테고리 삭제 시)
     */
    @Modifying
    @Query("DELETE FROM CategoryClosureJpaEntity cc WHERE cc.id.ancestorId = :ancestorId")
    void deleteAllByAncestorId(@Param("ancestorId") Long ancestorId);
}
