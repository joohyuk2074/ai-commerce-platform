package com.spartaecommerce.refund.infrastructure.persistence.jpa.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.refund.domain.entity.Refund;
import com.spartaecommerce.refund.domain.entity.RefundStatus;
import com.spartaecommerce.refund.application.dto.query.RefundSearchQuery;
import com.spartaecommerce.refund.domain.repository.RefundRepository;
import com.spartaecommerce.refund.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.spartaecommerce.refund.infrastructure.persistence.jpa.entity.QRefundJpaEntity.refundJpaEntity;

@Repository
@RequiredArgsConstructor
public class RefundRepositoryImpl implements RefundRepository {

    private final RefundJpaRepository refundJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(Refund refund) {
        RefundJpaEntity refundJpaEntity = RefundJpaEntity.from(refund);
        return refundJpaRepository.save(refundJpaEntity).getRefundId();
    }

    @Override
    public Refund getById(Long refundId) {
        RefundJpaEntity refundJpaEntity = refundJpaRepository.findById(refundId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "refundId: " + refundId
            ));

        return refundJpaEntity.toDomain();
    }

    @Override
    public Optional<Refund> findByOrderId(Long orderId) {
        return refundJpaRepository.findByOrderId(orderId)
            .map(RefundJpaEntity::toDomain);
    }

    @Override
    public Page<Refund> search(RefundSearchQuery searchQuery) {
        BooleanBuilder conditions = buildSearchConditions(searchQuery);

        Pageable pageable = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        List<Refund> refunds = fetchOrders(conditions, pageable);
        long total = countOrders(conditions);

        return new PageImpl<>(refunds, pageable, total);
    }

    private BooleanBuilder buildSearchConditions(RefundSearchQuery searchQuery) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userIdEquals(searchQuery.userId()));
        builder.and(statusEquals(searchQuery.refundStatus()));
        return builder;
    }

    private List<Refund> fetchOrders(BooleanBuilder conditions, Pageable pageable) {
        List<RefundJpaEntity> refundJpaEntities = queryFactory
            .selectFrom(refundJpaEntity)
            .where(conditions)
            .orderBy(refundJpaEntity.refundId.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return refundJpaEntities.stream()
            .map(RefundJpaEntity::toDomain)
            .toList();
    }

    private long countOrders(BooleanBuilder conditions) {
        Long count = queryFactory
            .select(refundJpaEntity.count())
            .where(conditions)
            .orderBy(refundJpaEntity.refundId.desc())
            .fetchOne();

        return count != null ? count : 0;
    }

    private BooleanExpression userIdEquals(Long userId) {
        return userId != null ? refundJpaEntity.userId.eq(userId) : null;
    }

    private BooleanExpression statusEquals(RefundStatus status) {
        return status != null ? refundJpaEntity.status.eq(status) : null;
    }
}