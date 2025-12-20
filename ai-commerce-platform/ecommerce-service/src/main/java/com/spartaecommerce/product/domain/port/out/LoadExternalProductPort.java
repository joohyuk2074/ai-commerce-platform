package com.spartaecommerce.product.domain.port.out;

import com.spartaecommerce.product.application.dto.result.ExternalProductSyncResult;

public interface LoadExternalProductPort {

    /**
     * 외부 API에서 상품 데이터를 조회합니다.
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 외부 상품 동기화 결과
     */
    ExternalProductSyncResult fetchExternalProducts(int page, int size);
}
