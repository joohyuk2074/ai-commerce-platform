package com.spartaecommerce.refund.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.port.out.LoadOrderPort;
import com.spartaecommerce.common.domain.product.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import com.spartaecommerce.product.domain.port.out.SaveProductPort;
import com.spartaecommerce.refund.application.dto.command.RefundCreateCommand;
import com.spartaecommerce.refund.application.dto.command.RefundProcessCommand;
import com.spartaecommerce.refund.domain.entity.Refund;
import com.spartaecommerce.refund.domain.entity.RefundStatus;
import com.spartaecommerce.refund.domain.port.in.RefundCommandUseCase;
import com.spartaecommerce.refund.domain.port.out.LoadRefundPort;
import com.spartaecommerce.refund.domain.port.out.SaveRefundPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RefundCommandService implements RefundCommandUseCase {

    private final LoadRefundPort loadRefundPort;
    private final SaveRefundPort saveRefundPort;
    private final LoadOrderPort loadOrderPort;
    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;

    @Override
    public Long create(RefundCreateCommand createCommand) {
        validateRefundCreation(createCommand.orderId());

        Refund refund = Refund.createNew(
            createCommand.userId(),
            createCommand.orderId(),
            createCommand.reason()
        );

        return saveRefundPort.save(refund);
    }

    @Override
    public void process(RefundProcessCommand processCommand) {
        Refund refund = loadRefundPort.getById(processCommand.refundId());

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

        saveRefundPort.save(refund);
    }

    private void validateRefundCreation(Long orderId) {
        Order order = loadOrderPort.getById(orderId);

        if (!order.isComplete()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Only completed orders can be refunded. orderStatus: " + order.getStatus()
            );
        }

        loadRefundPort.findByOrderId(orderId)
            .ifPresent(existingRefund -> {
                throw new BusinessException(
                    ErrorCode.ENTITY_ALREADY_EXISTS,
                    "refundId: " + existingRefund.getRefundId()
                );
            });
    }

    private void processApproval(Refund refund) {
        refund.approve();

        Order order = loadOrderPort.getById(refund.getOrderId());

        order.getOrderItems().forEach(orderItem -> {
            Product product = loadProductPort.getById(orderItem.getProductId());
            product.restoreQuantity(orderItem.getQuantity());
            saveProductPort.save(product);
        });
    }

    private void processRejection(Refund refund) {
        refund.reject();
    }
}
