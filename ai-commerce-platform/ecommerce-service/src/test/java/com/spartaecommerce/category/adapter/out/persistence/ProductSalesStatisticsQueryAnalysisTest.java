package com.spartaecommerce.category.adapter.out.persistence;

import com.spartaecommerce.category.application.dto.result.ProductSalesRankingResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

/**
 * 쿼리 실행 계획 분석을 위한 테스트
 *
 * 실행 방법:
 * 1. application-test.yml에 SQL 로깅 활성화
 * 2. 이 테스트 실행
 * 3. 콘솔에 출력된 SQL을 복사
 * 4. MySQL에서 EXPLAIN으로 실행
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductSalesStatisticsQueryAnalysisTest {

    @Autowired
    private ProductSalesStatisticsPersistenceAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void printQueryForExplain() {
        // 쿼리 실행 (SQL이 로그에 출력됨)
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        List<ProductSalesRankingResult> results =
            adapter.getProductSalesStatistics(startDate, endDate);

        // 출력된 SQL을 복사해서 MySQL에서 EXPLAIN 실행
        System.out.println("===== 결과 건수: " + results.size() + " =====");
    }

    @Test
    void analyzeQueryPlan() {
        // EXPLAIN을 직접 실행하는 방법
        String explainQuery = """
            EXPLAIN
            SELECT
                p.product_id,
                p.name,
                c.category_id,
                c.name,
                SUM(oi.quantity)
            FROM order_items oi
            INNER JOIN orders o ON oi.order_id = o.order_id
            INNER JOIN products p ON oi.product_id = p.product_id
            INNER JOIN categories c ON p.category_id = c.category_id
            WHERE o.status = 'COMPLETED'
              AND o.created_at BETWEEN ? AND ?
              AND p.deleted = false
              AND c.deleted = false
            GROUP BY p.product_id, p.name, c.category_id, c.name
            ORDER BY c.category_id ASC, SUM(oi.quantity) DESC
            """;

        // Native query로 EXPLAIN 실행
        @SuppressWarnings("unchecked")
        List<Object[]> explainResult = entityManager.createNativeQuery(explainQuery)
            .setParameter(1, "2024-01-01 00:00:00")
            .setParameter(2, "2024-12-31 23:59:59")
            .getResultList();

        System.out.println("===== EXPLAIN 결과 =====");
        explainResult.forEach(row -> {
            for (Object col : row) {
                System.out.print(col + " | ");
            }
            System.out.println();
        });
    }
}
