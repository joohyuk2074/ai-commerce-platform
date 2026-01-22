package com.spartaecommerce.category.application.service;

import com.spartaecommerce.category.application.dto.query.CategorySearchQuery;
import com.spartaecommerce.category.application.dto.query.TopSellingProductsQuery;
import com.spartaecommerce.category.application.dto.result.CategoryProductSalesRankingResult;
import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;
import com.spartaecommerce.category.application.dto.result.ProductSalesRankingResult;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.in.CategoryQueryUseCase;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.category.domain.port.out.LoadProductSalesStatisticsPort;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryQueryService implements CategoryQueryUseCase {

    private final LoadCategoryPort loadCategoryPort;
    private final LoadProductPort loadProductPort;
    private final LoadProductSalesStatisticsPort loadProductSalesStatisticsPort;

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

    @Override
    public List<CategoryProductSalesRankingResult> getTopSellingProductsByCategory(TopSellingProductsQuery query) {
        // 1. Repository에서 기간별 상품 판매량 집계 데이터 조회 (이미 카테고리별로 정렬되어 있음)
        List<ProductSalesRankingResult> productSales =
            loadProductSalesStatisticsPort.getProductSalesStatistics(query.startDate(), query.endDate());

        // 2. 카테고리별로 그룹화
        Map<Long, List<ProductSalesRankingResult>> groupedByCategory = productSales.stream()
            .collect(Collectors.groupingBy(ProductSalesRankingResult::getCategoryId));

        // 3. 각 카테고리별로 Top N 추출 및 순위 부여
        return groupedByCategory.entrySet().stream()
            .map(entry -> buildCategoryRanking(entry.getKey(), entry.getValue(), query.limit()))
            .sorted((c1, c2) -> Long.compare(c1.getCategoryId(), c2.getCategoryId()))
            .toList();
    }

    /**
     * 카테고리별 Top N 상품 순위 생성
     * DB에서 이미 판매량 순으로 정렬되어 있으므로 limit만 적용
     */
    private CategoryProductSalesRankingResult buildCategoryRanking(
        Long categoryId,
        List<ProductSalesRankingResult> products,
        Integer limit
    ) {
        String categoryName = products.isEmpty() ? "" : products.getFirst().getCategoryName();

        AtomicInteger rankCounter = new AtomicInteger(1);
        List<CategoryProductSalesRankingResult.ProductRankInfo> topProducts = products.stream()
            .limit(limit)  // DB에서 이미 정렬되어 있으므로 limit만 적용
            .map(product -> CategoryProductSalesRankingResult.ProductRankInfo.builder()
                .rank(rankCounter.getAndIncrement())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .totalSalesQuantity(product.getTotalSalesQuantity())
                .build()
            )
            .toList();

        return CategoryProductSalesRankingResult.builder()
            .categoryId(categoryId)
            .categoryName(categoryName)
            .topProducts(topProducts)
            .build();
    }
}
