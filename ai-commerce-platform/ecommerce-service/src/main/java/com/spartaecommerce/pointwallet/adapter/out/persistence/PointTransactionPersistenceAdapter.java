package com.spartaecommerce.pointwallet.adapter.out.persistence;

import com.spartaecommerce.pointwallet.adapter.out.persistence.jpa.entity.PointTransactionJpaEntity;
import com.spartaecommerce.pointwallet.adapter.out.persistence.jpa.repository.PointTransactionJpaRepository;
import com.spartaecommerce.pointwallet.domain.entity.PointTransaction;
import com.spartaecommerce.pointwallet.domain.entity.PointTransactionType;
import com.spartaecommerce.pointwallet.domain.port.out.LoadPointTransactionPort;
import com.spartaecommerce.pointwallet.domain.port.out.SavePointTransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointTransactionPersistenceAdapter implements LoadPointTransactionPort, SavePointTransactionPort {

    private final PointTransactionJpaRepository pointTransactionJpaRepository;

    @Override
    public Long save(PointTransaction transaction) {
        PointTransactionJpaEntity jpaEntity = PointTransactionJpaEntity.from(transaction);
        return pointTransactionJpaRepository.save(jpaEntity).getTransactionId();
    }

    @Override
    public List<PointTransaction> findByWalletId(Long walletId) {
        return pointTransactionJpaRepository.findByWalletIdOrderByCreatedAtDesc(walletId)
            .stream()
            .map(PointTransactionJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<PointTransaction> findExpiredTransactions(LocalDateTime now) {
        return pointTransactionJpaRepository.findExpiredTransactions(PointTransactionType.EARN, now)
            .stream()
            .map(PointTransactionJpaEntity::toDomain)
            .toList();
    }
}
