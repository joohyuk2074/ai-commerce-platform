package com.spartaecommerce.order.application;

import com.spartaecommerce.adapter.out.external.EcommerceServiceClient;
import com.spartaecommerce.domain.Passport;
import com.spartaecommerce.domain.vo.Money;
import com.spartaecommerce.order.domain.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 주문 포인트 처리 도메인 서비스 (MSA 버전)
 * - ecommerce-service의 PointWallet API를 통해 포인트 관리
 */
@Service
@RequiredArgsConstructor
public class OrderPointProcessor {

    private final EcommerceServiceClient ecommerceServiceClient;

    public BigDecimal calculateExpectedPoints(
        List<OrderItem> orderItems,
        Passport passport,
        Map<Long, Category> productIdToCategory
    ) {
        List<EcommerceServiceClient.OrderItemForCalculation> items = orderItems.stream()
            .map(item -> {
                Category category = productIdToCategory.get(item.getProductId());
                Long categoryId = category != null ? category.getCategoryId() : null;
                return new EcommerceServiceClient.OrderItemForCalculation(
                    item.getProductId(),
                    categoryId,
                    item.getTotalPrice().amount()
                );
            })
            .toList();

        return ecommerceServiceClient.calculatePoints(items, passport.grade());
    }

    public Money usePoints(Long userId, BigDecimal usePointAmount, Long orderId) {
        if (usePointAmount == null || usePointAmount.compareTo(BigDecimal.ZERO) == 0) {
            return Money.zero();
        }

        Money usePoints = Money.from(usePointAmount);
        ecommerceServiceClient.usePoints(
            userId,
            usePointAmount,
            "주문 사용 (Order ID: " + orderId + ")"
        );

        return usePoints;
    }

    public Money earnPoints(Long userId, BigDecimal earnedPoints, Long orderId) {
        if (earnedPoints == null || earnedPoints.compareTo(BigDecimal.ZERO) <= 0) {
            return Money.zero();
        }

        Money earnedPointsMoney = Money.from(earnedPoints);
        ecommerceServiceClient.earnPoints(
            userId,
            earnedPoints,
            "주문 적립 (Order ID: " + orderId + ")"
        );

        return earnedPointsMoney;
    }

    public void recordEarnTransaction(Long userId, Money earnedPoints, Long orderId) {
        // Transaction is already recorded by ecommerce-service
        // No additional action needed
    }

    public void recordUseTransaction(Long userId, Money usedPoints, Long orderId) {
        // Transaction is already recorded by ecommerce-service
        // No additional action needed
    }

    public void refundUsedPoints(Long userId, Money usedPointAmount, Long orderId) {
        if (usedPointAmount.isZero()) {
            return;
        }

        ecommerceServiceClient.earnPoints(
            userId,
            usedPointAmount.amount(),
            "주문 취소 - 사용 포인트 복구 (Order ID: " + orderId + ")"
        );
    }

    public void reclaimEarnedPoints(Long userId, Money earnedPointAmount, Long orderId) {
        if (earnedPointAmount.isZero()) {
            return;
        }

        ecommerceServiceClient.usePoints(
            userId,
            earnedPointAmount.amount(),
            "주문 취소 - 적립 포인트 회수 (Order ID: " + orderId + ")"
        );
    }
}
