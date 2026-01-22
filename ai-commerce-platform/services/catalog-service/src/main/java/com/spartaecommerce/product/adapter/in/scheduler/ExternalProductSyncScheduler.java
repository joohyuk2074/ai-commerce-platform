package com.spartaecommerce.product.adapter.in.scheduler;

import com.spartaecommerce.product.domain.port.in.ExternalProductSyncUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalProductSyncScheduler {

    private final ExternalProductSyncUseCase externalProductSyncUseCase;

    @Scheduled(cron = "0 0 * * * *")
    public void syncExternalProducts() {
        log.info("Starting scheduled external product synchronization");

        try {
            externalProductSyncUseCase.syncExternalProducts();
            log.info("Scheduled external product synchronization completed successfully");
        } catch (Exception e) {
            log.error("Scheduled external product synchronization failed", e);
        }
    }
}
