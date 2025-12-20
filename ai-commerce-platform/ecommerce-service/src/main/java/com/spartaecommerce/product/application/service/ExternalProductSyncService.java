package com.spartaecommerce.product.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.common.infrastructure.lock.DistributedLock;
import com.spartaecommerce.product.application.dto.result.ExternalProductSyncResult;
import com.spartaecommerce.product.application.dto.result.SyncBatchResult;
import com.spartaecommerce.product.application.processor.ExternalProductSyncProcessor;
import com.spartaecommerce.product.domain.port.in.ExternalProductSyncUseCase;
import com.spartaecommerce.product.domain.port.out.LoadExternalProductPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalProductSyncService implements ExternalProductSyncUseCase {

    private final LoadExternalProductPort loadExternalProductPort;
    private final ExternalProductSyncProcessor syncProcessor;

    @Value("${external.product.api.page-size:50}")
    private int pageSize;

    @Override
    @Async("externalSyncExecutor")
    @DistributedLock(
        key = "'external-product:sync'",
        waitTime = 3000L,
        leaseTime = 300000L, // 5분
        errorMessage = "이미 외부 상품 동기화가 진행 중입니다."
    )
    public void syncExternalProducts() {
        log.info("Starting external product synchronization with page size={}", pageSize);

        try {
            int totalNew = 0;
            int totalUpdated = 0;
            int currentPage = 0;
            boolean hasMore = true;

            while (hasMore) {
                log.info("Fetching page {} with size {}", currentPage, pageSize);

                // 외부 API에서 상품 데이터 조회 (트랜잭션 외부에서 실행)
                ExternalProductSyncResult result = loadExternalProductPort.fetchExternalProducts(currentPage, pageSize);

                if (result == null || result.products() == null || result.products().isEmpty()) {
                    log.warn("No products received from external API at page {}", currentPage);
                    break;
                }

                // 페이지 단위 배치 처리 (한 번의 트랜잭션으로 처리)
                try {
                    SyncBatchResult batchResult = syncProcessor.syncProductsBatch(result.products());

                    totalNew += batchResult.newCount();
                    totalUpdated += batchResult.updatedCount();

                    log.info("Page {} processed: new={}, updated={}",
                        currentPage, batchResult.newCount(), batchResult.updatedCount());
                } catch (Exception e) {
                    log.error("Failed to sync products batch at page {}: error={}",
                        currentPage, e.getMessage(), e);
                    // 페이지 단위 실패는 로그만 남기고 계속 진행
                }

                // 페이징 정보 확인하여 다음 페이지 존재 여부 판단
                if (result.pageInfo() != null && !result.pageInfo().last()) {
                    currentPage++;
                    log.info("Moving to next page: {}/{}", currentPage + 1, result.pageInfo().totalPages());
                } else {
                    hasMore = false;
                    log.info("Reached last page");
                }
            }

            log.info("External product synchronization completed: processedPages={}, totalNew={}, totalUpdated={}",
                currentPage + 1, totalNew, totalUpdated);
        } catch (Exception e) {
            log.error("External product synchronization failed", e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, e.getMessage());
        }
    }
}
