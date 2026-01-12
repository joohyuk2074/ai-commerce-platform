package com.spartaecommerce.user.adapter.out.persistence.jpa.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.repository.UserRepository;
import com.spartaecommerce.user.adapter.out.persistence.jpa.entity.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.spartaecommerce.user.adapter.out.persistence.jpa.entity.QUserJpaEntity.userJpaEntity;


@Primary
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(User user) {
        UserJpaEntity userJpaEntity = UserJpaEntity.from(user);
        return userJpaRepository.save(userJpaEntity).getUserId();
    }

    @Override
    public Optional<User> findById(Long userId) {
        UserJpaEntity entity = queryFactory
            .selectFrom(userJpaEntity)
            .where(
                idEquals(userId),
                isNotDeleted()
            )
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(UserJpaEntity::toDomain);
    }

    @Override
    public User getById(Long userId) {
        UserJpaEntity entity = queryFactory
            .selectFrom(userJpaEntity)
            .where(
                idEquals(userId),
                isNotDeleted()
            )
            .fetchOne();

        if (entity == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "userId: " + userId);
        }

        return entity.toDomain();
    }

    @Override
    public boolean existsById(Long userId) {
        Integer count = queryFactory
            .selectOne()
            .from(userJpaEntity)
            .where(
                idEquals(userId),
                isNotDeleted()
            )
            .fetchFirst();

        return count != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = queryFactory
            .selectOne()
            .from(userJpaEntity)
            .where(
                emailEquals(email),
                isNotDeleted()
            )
            .fetchFirst();

        return count != null;
    }

    @Override
    public Page<User> search(UserSearchQuery searchQuery) {
        BooleanExpression conditions = buildSearchConditions(searchQuery);

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(
            searchQuery.pageable().sortBy(),
            searchQuery.pageable().direction()
        );

        Pageable pageable = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        List<User> users = fetchUsers(conditions, orderSpecifier, pageable);
        long total = countUsers(conditions);

        return new PageImpl<>(users, pageable, total);
    }

    private BooleanExpression buildSearchConditions(UserSearchQuery searchQuery) {
        return emailContains(searchQuery.email()).or(nameContains(searchQuery.name()));
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortBy, String direction) {
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        return switch (sortBy.toLowerCase()) {
            case "id", "userId", "user_id" -> isAsc ? userJpaEntity.userId.asc() : userJpaEntity.userId.desc();
            case "createdAt", "created_at" -> isAsc ? userJpaEntity.createdAt.asc() : userJpaEntity.createdAt.desc();
            default -> userJpaEntity.createdAt.desc(); // 기본값: 최신순
        };
    }

    private List<User> fetchUsers(
        BooleanExpression conditions,
        OrderSpecifier<?> orderSpecifier,
        Pageable pageable
    ) {
        List<UserJpaEntity> userJpaEntities = queryFactory
            .selectFrom(userJpaEntity)
            .where(conditions)
            .orderBy(orderSpecifier)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return userJpaEntities.stream()
            .map(UserJpaEntity::toDomain)
            .toList();
    }

    private long countUsers(BooleanExpression conditions) {
        return queryFactory
            .select(userJpaEntity.count())
            .from(userJpaEntity)
            .where(conditions)
            .fetchOne();
    }

    private BooleanExpression idEquals(Long userId) {
        return userJpaEntity.userId.eq(userId);
    }

    private BooleanExpression emailEquals(String email) {
        return userJpaEntity.email.eq(email);
    }

    private BooleanExpression emailContains(String email) {
        return email != null ? userJpaEntity.email.contains(email) : null;
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? userJpaEntity.name.contains(name) : null;
    }

    private BooleanExpression isNotDeleted() {
        return userJpaEntity.deleted.isFalse();
    }
}
