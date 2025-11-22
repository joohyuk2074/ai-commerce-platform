package com.spartaecommerce.product.infrastructure.persistence.jpa.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;
import com.spartaecommerce.product.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.spartaecommerce.product.infrastructure.persistence.jpa.entity.QProductJpaEntity.productJpaEntity;


@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory queryFactory;


    @Override
    public Long save(Product product) {
        ProductJpaEntity productJpaEntity = ProductJpaEntity.from(product);
        return productJpaRepository.save(productJpaEntity).getProductId();
    }

    @Override
    public void saveAll(List<Product> products) {
        List<ProductJpaEntity> productJpaEntities = products.stream()
            .map(ProductJpaEntity::from)
            .toList();

        productJpaRepository.saveAll(productJpaEntities);
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = queryFactory
            .selectOne()
            .from(productJpaEntity)
            .where(
                nameEquals(name),
                isNotDeleted()
            )
            .fetchFirst();

        return count != null;
    }

    @Override
    public Optional<Product> findByName(String name) {
        ProductJpaEntity entity = queryFactory
            .selectFrom(productJpaEntity)
            .where(
                nameEquals(name),
                isNotDeleted()
            )
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(ProductJpaEntity::toDomain);
    }

    @Override
    public List<Product> findAllByProductIdIn(List<Long> productIds) {
        List<ProductJpaEntity> productJpaEntities = queryFactory
            .selectFrom(productJpaEntity)
            .where(
                productIdIn(productIds),
                isNotDeleted()
            )
            .fetch();

        return productJpaEntities.stream()
            .map(ProductJpaEntity::toDomain)
            .toList();
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        Integer count = queryFactory
            .selectOne()
            .from(productJpaEntity)
            .where(
                categoryIdEquals(categoryId),
                isNotDeleted()
            )
            .fetchFirst();

        return count != null;
    }

    @Override
    public Product getById(Long productId) {
        ProductJpaEntity entity = queryFactory
            .selectFrom(productJpaEntity)
            .where(
                productIdEquals(productId),
                isNotDeleted()
            )
            .fetchOne();

        if (entity == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "productId: " + productId);
        }

        return entity.toDomain();
    }

    @Override
    public Page<Product> search(ProductSearchQuery searchQuery) {
        BooleanBuilder conditions = buildSearchConditions(searchQuery);

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(
            searchQuery.pageable().sortBy(),
            searchQuery.pageable().direction()
        );

        Pageable pageable = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        List<Product> products = fetchProducts(conditions, orderSpecifier, pageable);
        long total = countProducts(conditions);

        return new PageImpl<>(products, pageable, total);
    }

    private BooleanBuilder buildSearchConditions(ProductSearchQuery searchQuery) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(isNotDeleted());
        builder.and(categoryIdEquals(searchQuery.categoryId()));
        builder.and(nameContains(searchQuery.keyword()));
        builder.and(priceInRange(searchQuery.minPrice(), searchQuery.maxPrice()));
        return builder;
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortBy, String direction) {
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        return switch (sortBy.toLowerCase()) {
            case "id", "productId", "product_id" ->
                isAsc ? productJpaEntity.productId.asc() : productJpaEntity.productId.desc();
            case "price" -> isAsc ? productJpaEntity.price.asc() : productJpaEntity.price.desc();
            case "name" -> isAsc ? productJpaEntity.name.asc() : productJpaEntity.name.desc();
            case "createdAt", "created_at" ->
                isAsc ? productJpaEntity.createdAt.asc() : productJpaEntity.createdAt.desc();
            default -> productJpaEntity.createdAt.desc(); // 기본값: 최신순
        };
    }

    private List<Product> fetchProducts(
        BooleanBuilder conditions,
        OrderSpecifier<?> orderSpecifier,
        Pageable pageable
    ) {
        List<ProductJpaEntity> productJpaEntities = queryFactory
            .selectFrom(productJpaEntity)
            .where(conditions)
            .orderBy(orderSpecifier)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return productJpaEntities.stream()
            .map(ProductJpaEntity::toDomain)
            .toList();
    }

    private long countProducts(BooleanBuilder conditions) {
        Long count = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(conditions)
            .fetchOne();

        return count != null ? count : 0L;
    }

    private BooleanExpression productIdEquals(Long productId) {
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "productId must not be null");
        }

        return productJpaEntity.productId.eq(productId);
    }

    private BooleanExpression nameEquals(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "name must not be null or blank");
        }

        return productJpaEntity.name.eq(name);
    }

    private BooleanExpression isNotDeleted() {
        return productJpaEntity.deleted.eq(false);
    }

    private BooleanExpression categoryIdEquals(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "categoryId must not be null");
        }

        return productJpaEntity.categoryId.eq(categoryId);
    }

    private BooleanExpression nameContains(String keyword) {
        return (keyword != null && !keyword.isEmpty())
            ? productJpaEntity.name.containsIgnoreCase(keyword)
            : null;
    }

    private static BooleanExpression productIdIn(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Expressions.FALSE;
        }

        return productJpaEntity.productId.in(productIds);
    }

    private BooleanExpression priceInRange(Money minPrice, Money maxPrice) {
        if (minPrice != null && maxPrice != null) {
            return productJpaEntity.price.between(minPrice.amount(), maxPrice.amount());
        } else if (minPrice != null) {
            return productJpaEntity.price.goe(minPrice.amount());
        } else if (maxPrice != null) {
            return productJpaEntity.price.loe(maxPrice.amount());
        }
        return null;
    }
}
