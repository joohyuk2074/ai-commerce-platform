package com.spartaecommerce.category.adapter.out.persistence;

import com.spartaecommerce.category.adapter.out.persistence.jpa.repository.CategoryClosureJpaRepository;
import com.spartaecommerce.category.domain.port.out.CategoryClosurePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 카테고리 Closure Table 초기화 컴포넌트
 *
 * 애플리케이션 시작 시 Closure Table이 비어있으면 자동으로 재구성합니다.
 * 운영 환경에서는 @Profile을 통해 수동으로 제어할 수 있습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod")  // 운영 환경에서는 수동 실행
public class CategoryClosureInitializer {

    private final CategoryClosureJpaRepository categoryClosureJpaRepository;
    private final CategoryClosurePort categoryClosurePort;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeCategoryClosureTable() {
        long count = categoryClosureJpaRepository.count();

        if (count == 0) {
            log.warn("Category closure table is empty. Rebuilding...");
            categoryClosurePort.rebuildClosureTable();
            log.info("Category closure table rebuild completed.");
        } else {
            log.info("Category closure table already initialized with {} entries.", count);
        }
    }
}
