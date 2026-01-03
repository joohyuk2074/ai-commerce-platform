package com.spartaecommerce.category.application.service;

import com.spartaecommerce.category.application.dto.query.CategorySearchQuery;
import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.in.CategoryQueryUseCase;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryQueryService implements CategoryQueryUseCase {

    private final LoadCategoryPort loadCategoryPort;
    private final LoadProductPort loadProductPort;

    @Override
    public List<Category> search(CategorySearchQuery searchQuery) {
        Product product = loadProductPort.getById(searchQuery.productId());
        Category category = loadCategoryPort.getById(product.getCategoryId());
        return List.of(category);
    }

    @Override
    @Cacheable(value = "categoryTree", unless = "#result == null || #result.isEmpty()")
    public List<CategoryTreeNodeResult> getCategoryTree() {
        // 1. 모든 카테고리를 한 번의 쿼리로 조회 (O(1) DB I/O)
        List<Category> allCategories = loadCategoryPort.findAll();

        // 2. 메모리에서 트리 구조 구성 (O(N))
        return buildTreeInMemory(allCategories);
    }

    /**
     * 메모리 내에서 카테고리 트리 구조를 구성
     * 시간복잡도: O(N), N = 전체 카테고리 수
     *
     * @param allCategories 모든 카테고리 목록
     * @return 최상위 카테고리 노드 리스트
     */
    private List<CategoryTreeNodeResult> buildTreeInMemory(List<Category> allCategories) {
        // 1. 모든 카테고리를 Map으로 변환 (빠른 조회를 위함)
        java.util.Map<Long, CategoryTreeNodeResult> nodeMap = new java.util.HashMap<>();

        for (Category category : allCategories) {
            nodeMap.put(category.getCategoryId(), CategoryTreeNodeResult.from(category));
        }

        // 2. 부모-자식 관계 설정 및 최상위 노드 수집
        List<CategoryTreeNodeResult> rootNodes = new java.util.ArrayList<>();

        for (CategoryTreeNodeResult node : nodeMap.values()) {
            if (node.getParentCategoryId() == null) {
                // 최상위 노드
                rootNodes.add(node);
            } else {
                // 부모에게 자식으로 추가
                CategoryTreeNodeResult parent = nodeMap.get(node.getParentCategoryId());
                if (parent != null) {
                    parent.addAllChildren(Collections.singletonList(node));
                }
            }
        }

        return rootNodes;
    }
}
