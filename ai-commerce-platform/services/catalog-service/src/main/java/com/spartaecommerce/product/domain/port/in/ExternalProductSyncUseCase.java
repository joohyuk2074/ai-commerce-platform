package com.spartaecommerce.product.domain.port.in;

public interface ExternalProductSyncUseCase {

    /**
     * 외부 API에서 상품 데이터를 가져와 비동기로 동기화합니다.
     * - 신규 상품은 추가
     * - 기존 상품은 업데이트
     * - 배치 처리로 트랜잭션 최적화
     */
    void syncExternalProducts();
}
