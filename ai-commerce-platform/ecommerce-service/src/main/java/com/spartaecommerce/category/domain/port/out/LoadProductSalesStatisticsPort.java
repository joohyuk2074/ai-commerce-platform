package com.spartaecommerce.category.domain.port.out;

import com.spartaecommerce.category.application.dto.result.ProductSalesRankingResult;

import java.time.LocalDate;
import java.util.List;

/**
 * 상품 판매 통계 조회 포트 (Outbound Port)
 */
public interface LoadProductSalesStatisticsPort {

    /**
     * 기간별 카테고리별 상품 판매량 집계 조회
     * 완료된 주문(COMPLETED)의 상품들을 카테고리별로 그룹화하여
     * 판매량을 집계합니다.
     *
     * @param startDate 시작 날짜 (inclusive)
     * @param endDate 종료 날짜 (inclusive)
     * @return 카테고리별 상품 판매 집계 리스트
     */
    List<ProductSalesRankingResult> getProductSalesStatistics(LocalDate startDate, LocalDate endDate);
}
