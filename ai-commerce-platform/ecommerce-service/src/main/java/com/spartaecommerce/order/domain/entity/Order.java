package com.spartaecommerce.order.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {

    private Long orderId;

    private Long userId;

    private OrderStatus status;

    private List<OrderItem> orderItems;

    private String shippingAddress;

    private Money usedPointAmount;

    private Money earnedPointAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Order createNew(Long userId, String shippingAddress) {
        return Order.builder()
            .userId(userId)
            .status(OrderStatus.PENDING)
            .orderItems(new ArrayList<>())
            .shippingAddress(shippingAddress)
            .usedPointAmount(Money.zero())
            .earnedPointAmount(Money.zero())
            .build();
    }

    public void setPointAmounts(Money usedPointAmount, Money earnedPointAmount) {
        this.usedPointAmount = usedPointAmount;
        this.earnedPointAmount = earnedPointAmount;
    }

    public void addOrderItem(
        Long productId,
        String productName,
        Money price,
        Integer quantity
    ) {
        OrderItem orderItem = OrderItem.createNew(
            productId,
            productName,
            price,
            quantity
        );
        this.orderItems.add(orderItem);
    }

    public BigDecimal calculateTotalAmount() {
        Money totalAmount = this.orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(Money.zero(), Money::add);

        return totalAmount.amount();
    }

    public void updateOrderStatus(OrderStatus newStatus) {
        validateStatusTransition(this.status, newStatus);
        this.status = newStatus;
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELED) {
            throw new BusinessException(
                ErrorCode.ORDER_INVALID_STATE_TRANSITION,
                "This order has already been canceled."
            );
        }

        validateStatusTransition(this.status, OrderStatus.CANCELED);
        this.status = OrderStatus.CANCELED;
    }

    public boolean isComplete() {
        return this.status == OrderStatus.COMPLETED;
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return;
        }

        if (from == OrderStatus.PENDING) {
            if (to == OrderStatus.COMPLETED || to == OrderStatus.CANCELED) {
                return;
            }

            throw new BusinessException(
                ErrorCode.ORDER_INVALID_STATE_TRANSITION,
                from + " -> " + to
            );
        }

        throw new BusinessException(
            ErrorCode.ORDER_INVALID_STATE_TRANSITION,
            from + " -> " + to
        );
    }
}
