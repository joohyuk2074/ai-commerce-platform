package com.spartaecommerce.refund.application;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.repository.OrderRepository;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;
import com.spartaecommerce.refund.application.dto.result.RefundResult;
import com.spartaecommerce.refund.application.dto.command.RefundCreateCommand;
import com.spartaecommerce.refund.application.dto.command.RefundProcessCommand;
import com.spartaecommerce.refund.application.dto.query.RefundSearchQuery;
import com.spartaecommerce.refund.domain.entity.Refund;
import com.spartaecommerce.refund.domain.entity.RefundStatus;
import com.spartaecommerce.refund.domain.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Long create(RefundCreateCommand createCommand) {
        validateRefundCreation(createCommand.orderId());

        Refund refund = Refund.createNew(
            createCommand.userId(),
            createCommand.orderId(),
            createCommand.reason()
        );

        return refundRepository.save(refund);
    }

    @Transactional
    public void process(RefundProcessCommand processCommand) {
        Refund refund = refundRepository.getById(processCommand.refundId());

        if (processCommand.status() == RefundStatus.APPROVED) {
            processApproval(refund);
        } else if (processCommand.status() == RefundStatus.REJECTED) {
            processRejection(refund);
        } else {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Invalid refund status: " + processCommand.status()
            );
        }

        refundRepository.save(refund);
    }

    public Page<RefundResult> search(RefundSearchQuery searchQuery) {
        Page<Refund> refunds = refundRepository.search(searchQuery);
        return refunds.map(RefundResult::from);
    }

    private void validateRefundCreation(Long orderId) {
        Order order = orderRepository.getById(orderId);

        if (!order.isComplete()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Only completed orders can be refunded. orderStatus: " + order.getStatus()
            );
        }

        refundRepository.findByOrderId(orderId)
            .ifPresent(existingRefund -> {
                throw new BusinessException(
                    ErrorCode.ENTITY_ALREADY_EXISTS,
                    "refundId: " + existingRefund.getRefundId()
                );
            });
    }

    private void processApproval(Refund refund) {
        refund.approve();

        Order order = orderRepository.getById(refund.getOrderId());

        order.getOrderItems().forEach(orderItem -> {
            Product product = productRepository.getById(orderItem.getProductId());
            product.restoreQuantity(orderItem.getQuantity());
            productRepository.save(product);
        });
    }

    private void processRejection(Refund refund) {
        refund.reject();
    }
}