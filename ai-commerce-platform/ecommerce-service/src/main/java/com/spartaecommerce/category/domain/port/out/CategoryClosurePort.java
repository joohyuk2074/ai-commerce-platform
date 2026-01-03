package com.spartaecommerce.category.domain.port.out;

public interface CategoryClosurePort {

    /**
     * 새 카테고리에 대한 Closure Table 엔트리 생성
     *
     * 생성 규칙:
     * 1. 자기 자신과의 관계 (depth=0)
     * 2. 부모의 모든 조상과의 관계 (부모의 depth + 1)
     *
     * @param categoryId 새로 생성된 카테고리 ID
     * @param parentCategoryId 부모 카테고리 ID (null이면 최상위)
     */
    void insertClosureForNewCategory(Long categoryId, Long parentCategoryId);

    /**
     * 카테고리 삭제 시 관련된 모든 Closure Table 엔트리 삭제
     *
     * @param categoryId 삭제할 카테고리 ID
     */
    void deleteClosureForCategory(Long categoryId);

    /**
     * 전체 Closure Table 재구성 (마이그레이션용)
     * 기존 데이터를 기반으로 Closure Table을 새로 생성
     */
    void rebuildClosureTable();
}
