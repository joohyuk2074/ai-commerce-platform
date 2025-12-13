package com.spartaecommerce.category.adapter.out.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.category.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import com.spartaecommerce.category.adapter.out.persistence.jpa.repository.CategoryJpaRepository;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.spartaecommerce.category.adapter.out.persistence.jpa.entity.QCategoryJpaEntity.categoryJpaEntity;

@Repository
@RequiredArgsConstructor
public class CategoryQueryPersistenceAdapter implements LoadCategoryPort {

    private final CategoryJpaRepository categoryJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Category> findById(Long categoryId) {
        CategoryJpaEntity entity = queryFactory
            .selectFrom(categoryJpaEntity)
            .where(
                categoryIdEquals(categoryId),
                isNotDeleted()
            )
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(CategoryJpaEntity::toDomain);
    }

    @Override
    public Category getById(Long categoryId) {
        return findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "categoryId: " + categoryId));
    }

    @Override
    public boolean existsById(Long categoryId) {
        Integer count = queryFactory
            .selectOne()
            .from(categoryJpaEntity)
            .where(
                categoryIdEquals(categoryId),
                isNotDeleted()
            )
            .fetchFirst();

        return count != null;
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = queryFactory
            .selectOne()
            .from(categoryJpaEntity)
            .where(
                nameEquals(name),
                isNotDeleted()
            )
            .fetchFirst();

        return count != null;
    }

    @Override
    public boolean hasActiveChildren(Long parentId) {
        Integer count = queryFactory
            .selectOne()
            .from(categoryJpaEntity)
            .where(
                categoryJpaEntity.parent.categoryId.eq(parentId),
                isNotDeleted()
            )
            .fetchFirst();

        return count != null;
    }

    @Override
    public List<Category> findAllByParentId(Long categoryId) {
        List<CategoryJpaEntity> categoryEntities = queryFactory
            .selectFrom(categoryJpaEntity)
            .where(
                isParentIdEquals(categoryId),
                isNotDeleted()
            )
            .fetch();

        return categoryEntities.stream()
            .map(CategoryJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Category> findAllByCategoryIdIn(Set<Long> categoryIds) {
        List<CategoryJpaEntity> categoryJpaEntities = queryFactory
            .selectFrom(categoryJpaEntity)
            .where(
                categoryJpaEntity.categoryId.in(categoryIds),
                isNotDeleted()
            )
            .fetch();

        return categoryJpaEntities.stream()
            .map(CategoryJpaEntity::toDomain)
            .toList();
    }

    private static BooleanExpression categoryIdEquals(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "categoryId must not be null");
        }

        return categoryJpaEntity.categoryId.eq(categoryId);
    }

    private BooleanExpression isNotDeleted() {
        return categoryJpaEntity.deleted.isFalse();
    }

    private static BooleanExpression nameEquals(String name) {
        if (name == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "name must not be null");
        }

        return categoryJpaEntity.name.eq(name);
    }

    private BooleanExpression isParentIdEquals(Long categoryId) {
        if (categoryId == null) {
            return categoryJpaEntity.parent.categoryId.isNull();
        }
        return categoryJpaEntity.parent.categoryId.eq(categoryId);
    }
}
