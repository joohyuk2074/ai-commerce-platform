package com.spartaecommerce.user.adapter.out.persistence.jpa.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.user.adapter.out.persistence.jpa.entity.UserJpaEntity;
import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.SearchUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.spartaecommerce.user.adapter.out.persistence.jpa.entity.QUserJpaEntity.userJpaEntity;


@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements SearchUserPort {

    private final JPAQueryFactory queryFactory;

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
