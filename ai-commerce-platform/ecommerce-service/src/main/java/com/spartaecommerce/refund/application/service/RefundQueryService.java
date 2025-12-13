package com.spartaecommerce.refund.application.service;

import com.spartaecommerce.refund.application.dto.query.RefundSearchQuery;
import com.spartaecommerce.refund.application.dto.result.RefundResult;
import com.spartaecommerce.refund.domain.entity.Refund;
import com.spartaecommerce.refund.domain.port.in.RefundQueryUseCase;
import com.spartaecommerce.refund.domain.port.out.LoadRefundPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefundQueryService implements RefundQueryUseCase {

    private final LoadRefundPort loadRefundPort;

    @Override
    public Page<RefundResult> search(RefundSearchQuery searchQuery) {
        Page<Refund> refunds = loadRefundPort.search(searchQuery);
        return refunds.map(RefundResult::from);
    }
}
