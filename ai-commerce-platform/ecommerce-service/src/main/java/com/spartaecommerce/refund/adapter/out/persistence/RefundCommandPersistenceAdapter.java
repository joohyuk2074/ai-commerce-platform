package com.spartaecommerce.refund.adapter.out.persistence;

import com.spartaecommerce.refund.adapter.out.persistence.jpa.entity.RefundJpaEntity;
import com.spartaecommerce.refund.adapter.out.persistence.jpa.repository.RefundJpaRepository;
import com.spartaecommerce.refund.domain.entity.Refund;
import com.spartaecommerce.refund.domain.port.out.SaveRefundPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefundCommandPersistenceAdapter implements SaveRefundPort {

    private final RefundJpaRepository refundJpaRepository;

    @Override
    public Long save(Refund refund) {
        RefundJpaEntity refundJpaEntity = RefundJpaEntity.from(refund);
        return refundJpaRepository.save(refundJpaEntity).getRefundId();
    }
}
