package com.spartaecommerce.product.application.processor;

import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.application.dto.result.ExternalProductSyncResult;
import com.spartaecommerce.product.application.dto.result.SyncBatchResult;
import com.spartaecommerce.product.domain.entity.ExternalProductRef;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import com.spartaecommerce.product.domain.port.out.SaveProductPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalProductSyncProcessor {

    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;
    private final LoadCategoryPort loadCategoryPort;

    /**
     * 여러 상품을 배치로 동기화 (페이지 단위 처리)
     *
     * @param items 외부 상품 정보 리스트
     * @return 동기화 결과 (신규 개수, 업데이트 개수)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SyncBatchResult syncProductsBatch(List<ExternalProductSyncResult.ExternalProductItem> items) {
        if (items == null || items.isEmpty()) {
            return new SyncBatchResult(0, 0);
        }

        // 1. 외부 참조 ID로 기존 상품 조회
        List<ExternalProductRef> refs = items.stream()
            .map(item -> ExternalProductRef.of(item.vendor(), item.externalId()))
            .toList();

        Map<ExternalProductRef, Product> existingProducts =
            loadProductPort.findAllByExternalProductRefs(refs).stream()
                .collect(Collectors.toMap(Product::getExternalProductRef, p -> p));

        // 2. 신규/업데이트 분류
        List<Product> productsToUpdate = new ArrayList<>();
        List<Product> productsToCreate = new ArrayList<>();
        Category externalCategory = null;

        for (ExternalProductSyncResult.ExternalProductItem item : items) {
            ExternalProductRef ref = ExternalProductRef.of(item.vendor(), item.externalId());
            Product existingProduct = existingProducts.get(ref);

            if (externalCategory == null) {
                externalCategory = loadCategoryPort.getExternalCategory();
            }

            if (existingProduct != null) {
                // 기존 상품 업데이트
                existingProduct.update(
                    item.name(),
                    item.description(),
                    Money.from(item.price()),
                    item.stock(),
                    externalCategory.getCategoryId()
                );
                productsToUpdate.add(existingProduct);
            } else {
                // 신규 상품 생성 (외부 동기화 상품은 sellerId = null)

                Product newProduct = Product.createFromExternal(
                    item.vendor(),
                    item.externalId(),
                    item.name(),
                    item.description(),
                    Money.from(item.price()),
                    item.stock(),
                    externalCategory.getCategoryId(),
                    null  // External products have no seller
                );
                productsToCreate.add(newProduct);
            }
        }

        // 3. 배치 저장
        if (!productsToUpdate.isEmpty()) {
            saveProductPort.saveAll(productsToUpdate);
            log.debug("Updated {} products in batch", productsToUpdate.size());
        }

        if (!productsToCreate.isEmpty()) {
            saveProductPort.saveAll(productsToCreate);
            log.debug("Created {} products in batch", productsToCreate.size());
        }

        return new SyncBatchResult(productsToCreate.size(), productsToUpdate.size());
    }
}
