package com.spartaecommerce.product.domain.port.out;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ProductFakeRepository implements LoadProductPort, SaveProductPort {

    private final Map<Long, Product> repository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(Product product) {
        if (product.getProductId() == null) {
            long productId = idGenerator.getAndIncrement();
            Product newProduct = Product.builder()
                .productId(productId)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(product.getCategoryId())
                .deleted(product.isDeleted())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(productId, newProduct);
            return productId;
        } else {
            Product updatedProduct = Product.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(product.getCategoryId())
                .deleted(product.isDeleted())
                .createdAt(product.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(product.getProductId(), updatedProduct);
            return product.getProductId();
        }
    }

    @Override
    public void saveAll(List<Product> products) {
        products.forEach(this::save);
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return Optional.ofNullable(repository.get(productId))
            .filter(product -> !product.isDeleted());
    }

    @Override
    public Product getById(Long productId) {
        return findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "productId: " + productId));
    }

    @Override
    public Optional<Product> findByName(String name) {
        return repository.values().stream()
            .filter(product -> !product.isDeleted())
            .filter(product -> product.getName().equals(name))
            .findFirst();
    }

    @Override
    public boolean existsByName(String name) {
        return repository.values().stream()
            .filter(product -> !product.isDeleted())
            .anyMatch(product -> product.getName().equals(name));
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return repository.values().stream()
            .filter(product -> !product.isDeleted())
            .anyMatch(product -> product.getCategoryId().equals(categoryId));
    }

    @Override
    public Page<Product> search(ProductSearchQuery searchQuery) {
        Stream<Product> stream = repository.values().stream()
            .filter(product -> !product.isDeleted());

        // 카테고리 필터
        if (searchQuery.categoryId() != null) {
            stream = stream.filter(product -> product.getCategoryId().equals(searchQuery.categoryId()));
        }

        // 키워드 필터
        if (searchQuery.keyword() != null && !searchQuery.keyword().isEmpty()) {
            stream = stream.filter(product ->
                product.getName().toLowerCase().contains(searchQuery.keyword().toLowerCase()));
        }

        // 가격 범위 필터
        if (searchQuery.minPrice() != null) {
            stream = stream.filter(product ->
                product.getPrice().amount().compareTo(searchQuery.minPrice().amount()) >= 0);
        }
        if (searchQuery.maxPrice() != null) {
            stream = stream.filter(product ->
                product.getPrice().amount().compareTo(searchQuery.maxPrice().amount()) <= 0);
        }

        List<Product> filteredProducts = stream.toList();

        // 정렬
        Comparator<Product> comparator = getComparator(
            searchQuery.pageable().sortBy(),
            searchQuery.pageable().direction()
        );
        List<Product> sortedProducts = filteredProducts.stream()
            .sorted(comparator)
            .toList();

        PageRequest pageRequest = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), sortedProducts.size());

        List<Product> pageContent = sortedProducts.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, sortedProducts.size());
    }

    @Override
    public List<Product> findAllByProductIdIn(List<Long> productIds) {
        return repository.values().stream()
            .filter(product -> !product.isDeleted())
            .filter(product -> productIds.contains(product.getProductId()))
            .toList();
    }

    private Comparator<Product> getComparator(String sortBy, String direction) {
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        return switch (sortBy.toLowerCase()) {
            case "id", "productid", "product_id" -> isAsc
                ? Comparator.comparing(Product::getProductId)
                : Comparator.comparing(Product::getProductId).reversed();
            case "price" -> isAsc
                ? Comparator.comparing(p -> p.getPrice().amount())
                : Comparator.comparing((Product p) -> p.getPrice().amount()).reversed();
            case "name" -> isAsc
                ? Comparator.comparing(Product::getName)
                : Comparator.comparing(Product::getName).reversed();
            case "createdat", "created_at" -> isAsc
                ? Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                : Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    public void clear() {
        repository.clear();
        idGenerator.set(1L);
    }
}
