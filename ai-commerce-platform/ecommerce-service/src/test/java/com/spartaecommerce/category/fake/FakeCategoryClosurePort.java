package com.spartaecommerce.category.fake;

import com.spartaecommerce.category.domain.port.out.CategoryClosurePort;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 테스트용 In-Memory Category Closure Table
 */
public class FakeCategoryClosurePort implements CategoryClosurePort {

    // ancestor -> descendant -> depth 매핑
    private final Map<Long, Map<Long, Integer>> closureTable = new ConcurrentHashMap<>();

    @Override
    public void insertClosureForNewCategory(Long categoryId, Long parentCategoryId) {
        // 1. 자기 자신과의 관계 (depth=0)
        closureTable.computeIfAbsent(categoryId, k -> new ConcurrentHashMap<>())
            .put(categoryId, 0);

        // 2. 부모가 있으면 모든 조상과의 관계 추가
        if (parentCategoryId != null) {
            // 모든 조상을 찾아서 새 카테고리와의 관계 추가
            for (Map.Entry<Long, Map<Long, Integer>> entry : closureTable.entrySet()) {
                Long ancestorId = entry.getKey();
                Map<Long, Integer> descendants = entry.getValue();

                // 이 조상의 자손에 부모가 있으면, 새 카테고리도 이 조상의 자손
                if (descendants.containsKey(parentCategoryId)) {
                    Integer parentDepth = descendants.get(parentCategoryId);
                    descendants.put(categoryId, parentDepth + 1);
                }
            }
        }
    }

    @Override
    public void deleteClosureForCategory(Long categoryId) {
        // 해당 카테고리와 관련된 모든 관계 삭제
        closureTable.remove(categoryId);

        // 다른 카테고리의 자손으로서의 관계도 삭제
        for (Map<Long, Integer> descendants : closureTable.values()) {
            descendants.remove(categoryId);
        }
    }

    @Override
    public void rebuildClosureTable() {
        closureTable.clear();
    }

    // 테스트 헬퍼 메서드
    public boolean hasRelation(Long ancestorId, Long descendantId) {
        Map<Long, Integer> descendants = closureTable.get(ancestorId);
        return descendants != null && descendants.containsKey(descendantId);
    }

    public Integer getDepth(Long ancestorId, Long descendantId) {
        Map<Long, Integer> descendants = closureTable.get(ancestorId);
        return descendants != null ? descendants.get(descendantId) : null;
    }

    public Set<Long> getDescendants(Long ancestorId) {
        Map<Long, Integer> descendants = closureTable.get(ancestorId);
        return descendants != null ? new HashSet<>(descendants.keySet()) : Collections.emptySet();
    }

    public void clear() {
        closureTable.clear();
    }
}
