package com.spartaecommerce.category.fake;

import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.common.domain.product.ExternalProductRef;
import com.spartaecommerce.common.domain.product.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 테스트용 In-Memory Product Repository
 */
public class FakeLoadProductPort implements LoadProductPort {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> categoryProductIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<Product> findById(Long productId) {
        return Optional.ofNullable(store.get(productId));
    }

    @Override
    public Product getById(Long productId) {
        return findById(productId)
            .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));
    }

    @Override
    public Optional<Product> findByName(String name) {
        return store.values().stream()
            .filter(product -> product.getName().equals(name))
            .findFirst();
    }

    @Override
    public boolean existsByName(String name) {
        return store.values().stream()
            .anyMatch(product -> product.getName().equals(name));
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        Set<Long> productIds = categoryProductIndex.get(categoryId);
        return productIds != null && !productIds.isEmpty();
    }

    @Override
    public Page<Product> search(ProductSearchQuery searchQuery) {
        List<Product> result = new ArrayList<>(store.values());
        return new PageImpl<>(result);
    }

    @Override
    public List<Product> findAllByProductIdIn(List<Long> productIds) {
        return productIds.stream()
            .map(store::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<Product> findAllByExternalProductRefs(List<ExternalProductRef> externalProductRefs) {
        return Collections.emptyList();
    }

    // 테스트 헬퍼 메서드
    public void addProduct(Product product, Long categoryId) {
        store.put(product.getProductId(), product);
        if (categoryId != null) {
            categoryProductIndex.computeIfAbsent(categoryId, k -> new HashSet<>())
                .add(product.getProductId());
        }
    }

    public void clear() {
        store.clear();
        categoryProductIndex.clear();
    }
}
