package com.spartaecommerce.product.adapter.out.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.domain.entity.ExternalProductRef;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.spartaecommerce.product.adapter.out.persistence.jpa.entity.QProductJpaEntity.productJpaEntity;

@Repository
@RequiredArgsConstructor
public class ProductPersistenceQueryAdapter implements LoadProductPort {
    private final JPAQueryFactory queryFactory;

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
    public Optional<Product> findById(Long productId) {
        ProductJpaEntity entity = queryFactory
            .selectFrom(productJpaEntity)
            .where(
                productIdEquals(productId),
                isNotDeleted()
            )
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(ProductJpaEntity::toDomain);
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
    public List<Product> findAllByExternalProductRefs(List<ExternalProductRef> externalProductRefs) {
        if (externalProductRefs == null || externalProductRefs.isEmpty()) {
            return List.of();
        }

        BooleanBuilder builder = new BooleanBuilder();
        for (ExternalProductRef ref : externalProductRefs) {
            builder.or(vendorAndExternalIdEquals(ref));
        }

        List<ProductJpaEntity> entities = queryFactory
            .selectFrom(productJpaEntity)
            .where(
                builder,
                isNotDeleted()
            )
            .fetch();

        return entities.stream()
            .map(ProductJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Page<Product> search(ProductSearchQuery searchQuery) {
        BooleanBuilder conditions = buildSearchConditions(searchQuery);

        Pageable pageable = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        List<Product> products = fetchProducts(conditions, pageable);
        long total = countProducts(conditions);

        return new PageImpl<>(products, pageable, total);
    }

    private BooleanBuilder buildSearchConditions(ProductSearchQuery searchQuery) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(isNotDeleted());
        builder.and(categoryIdEquals(searchQuery.categoryId()));
        builder.and(isOrderableEquals(searchQuery.isOrderable()));
        builder.and(nameContains(searchQuery.keyword()));
        builder.and(priceInRange(searchQuery.minPrice(), searchQuery.maxPrice()));
        return builder;
    }

    private List<Product> fetchProducts(
        BooleanBuilder conditions,
        Pageable pageable
    ) {
        List<ProductJpaEntity> productJpaEntities = queryFactory
            .selectFrom(productJpaEntity)
            .where(conditions)
            .orderBy(productJpaEntity.productId.desc())
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

    private BooleanExpression externalIdEquals(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "externalId must not be null or blank");
        }

        return productJpaEntity.externalId.eq(externalId);
    }

    private BooleanExpression vendorAndExternalIdEquals(ExternalProductRef externalProductRef) {
        if (externalProductRef == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "externalProductRef must not be null");
        }

        return productJpaEntity.vendor.eq(externalProductRef.vendor())
            .and(productJpaEntity.externalId.eq(externalProductRef.externalId()));
    }

    private BooleanExpression isNotDeleted() {
        return productJpaEntity.deleted.eq(false);
    }

    private BooleanExpression categoryIdEquals(Long categoryId) {
        return (categoryId != null)
            ? productJpaEntity.categoryId.eq(categoryId)
            : null;
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

    private BooleanExpression isOrderableEquals(Boolean isOrderable) {
        return (isOrderable != null)
            ? productJpaEntity.isOrderable.eq(isOrderable)
            : null;
    }
}
