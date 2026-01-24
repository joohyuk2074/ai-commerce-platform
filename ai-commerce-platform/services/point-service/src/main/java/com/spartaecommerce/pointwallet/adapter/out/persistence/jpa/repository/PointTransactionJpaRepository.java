package com.spartaecommerce.pointwallet.adapter.out.persistence.jpa.repository;

import com.spartaecommerce.pointwallet.adapter.out.persistence.jpa.entity.PointTransactionJpaEntity;
import com.spartaecommerce.common.domain.pointwallet.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransactionJpaEntity, Long> {

    List<PointTransactionJpaEntity> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    @Query("SELECT pt FROM PointTransactionJpaEntity pt " +
           "WHERE pt.type = :type " +
           "AND pt.expireAt IS NOT NULL " +
           "AND pt.expireAt <= :now " +
           "ORDER BY pt.createdAt ASC")
    List<PointTransactionJpaEntity> findExpiredTransactions(
        @Param("type") PointTransactionType type,
        @Param("now") LocalDateTime now
    );
}
