package com.spartaecommerce.refund.domain.port.out;

import com.spartaecommerce.refund.application.dto.query.RefundSearchQuery;
import com.spartaecommerce.refund.domain.entity.Refund;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * 환불 조회 포트 (Outbound Port)
 */
public interface LoadRefundPort {

    Optional<Refund> findById(Long refundId);

    Refund getById(Long refundId);

    Optional<Refund> findByOrderId(Long orderId);

    Page<Refund> search(RefundSearchQuery searchQuery);
}
