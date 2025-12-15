package com.spartaecommerce.order.application;

import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.util.DateTimeHolder;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.pointwallet.domain.entity.PointPolicy;
import com.spartaecommerce.pointwallet.domain.entity.PointTransaction;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.port.out.LoadPointTransactionPort;
import com.spartaecommerce.pointwallet.domain.port.out.SavePointTransactionPort;
import com.spartaecommerce.pointwallet.domain.port.out.LoadPointWalletPort;
import com.spartaecommerce.pointwallet.domain.port.out.SavePointWalletPort;
import com.spartaecommerce.pointwallet.domain.service.PointCalculator;
import com.spartaecommerce.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 주문 포인트 처리 도메인 서비스
 * - 포인트 계산
 * - 포인트 적립 및 사용
 * - 포인트 트랜잭션 기록
 */
@Service
@RequiredArgsConstructor
public class OrderPointProcessor {

    private final PointCalculator pointCalculator;
    private final LoadPointWalletPort loadPointWalletPort;
    private final SavePointWalletPort savePointWalletPort;
    private final SavePointTransactionPort savePointTransactionPort;
    private final PointsProperties pointsProperties;
    private final DateTimeHolder dateTimeHolder;

    public BigDecimal calculateExpectedPoints(
        List<OrderItem> orderItems,
        User user,
        Map<Long, Category> productIdToCategory
    ) {
        PointPolicy pointPolicy = new PointPolicy(
            pointsProperties.getDefaultRate(),
            pointsProperties.getVipRate(),
            pointsProperties.getCategoryRateMapAsBigDecimal(),
            BigDecimal.ZERO
        );

        return pointCalculator.calculateExpectedPoints(
            orderItems,
            user,
            productIdToCategory,
            pointPolicy
        );
    }

    public Money usePoints(Long userId, BigDecimal usePointAmount) {
        if (usePointAmount == null || usePointAmount.compareTo(BigDecimal.ZERO) == 0) {
            return Money.zero();
        }

        PointWallet wallet = loadPointWalletPort.getByUserId(userId);
        Money usePoints = Money.from(usePointAmount);
        wallet.usePoints(usePoints, pointsProperties.getMinUsagePoint());
        savePointWalletPort.save(wallet);

        return usePoints;
    }

    public Money earnPoints(Long userId, BigDecimal earnedPoints) {
        if (earnedPoints == null || earnedPoints.compareTo(BigDecimal.ZERO) <= 0) {
            return Money.zero();
        }

        PointWallet wallet = loadPointWalletPort.getByUserId(userId);
        Money earnedPointsMoney = Money.from(earnedPoints);
        Money maxBalance = Money.from(pointsProperties.getMaxBalance());

        wallet.earnPoints(earnedPointsMoney, maxBalance);
        savePointWalletPort.save(wallet);

        return earnedPointsMoney;
    }

    public void recordEarnTransaction(Long userId, Money earnedPoints, Long orderId) {
        PointWallet wallet = loadPointWalletPort.getByUserId(userId);
        LocalDateTime pointExpireAt = dateTimeHolder.getCurrentDateTime().plusYears(1);

        PointTransaction pointTransaction = PointTransaction.createEarn(
            wallet.getWalletId(),
            earnedPoints,
            wallet.getBalance(),
            pointExpireAt,
            "주문 적립 (Order ID: " + orderId + ")"
        );
        savePointTransactionPort.save(pointTransaction);
    }

    public void recordUseTransaction(Long userId, Money usedPoints, Long orderId) {
        if (usedPoints.isZero()) {
            return;
        }

        PointWallet wallet = loadPointWalletPort.getByUserId(userId);
        PointTransaction pointTransaction = PointTransaction.createUse(
            wallet.getWalletId(),
            usedPoints,
            wallet.getBalance(),
            "주문 사용 (Order ID: " + orderId + ")"
        );
        savePointTransactionPort.save(pointTransaction);
    }

    public void refundUsedPoints(Long userId, Money usedPointAmount, Long orderId) {
        if (usedPointAmount.isZero()) {
            return;
        }

        PointWallet wallet = loadPointWalletPort.getByUserId(userId);
        Money maxBalance = Money.from(pointsProperties.getMaxBalance());

        // 포인트 복구 (적립)
        wallet.earnPoints(usedPointAmount, maxBalance);
        savePointWalletPort.save(wallet);

        // 트랜잭션 기록
        LocalDateTime pointExpireAt = dateTimeHolder.getCurrentDateTime().plusYears(1);
        PointTransaction pointTransaction = PointTransaction.createEarn(
            wallet.getWalletId(),
            usedPointAmount,
            wallet.getBalance(),
            pointExpireAt,
            "주문 취소 - 사용 포인트 복구 (Order ID: " + orderId + ")"
        );
        savePointTransactionPort.save(pointTransaction);
    }

    public void reclaimEarnedPoints(Long userId, Money earnedPointAmount, Long orderId) {
        if (earnedPointAmount.isZero()) {
            return;
        }

        PointWallet wallet = loadPointWalletPort.getByUserId(userId);

        // 포인트 회수 (차감) - 최소 사용 포인트 제한 없이 차감
        wallet.usePoints(earnedPointAmount, 0);
        savePointWalletPort.save(wallet);

        // 트랜잭션 기록
        PointTransaction pointTransaction = PointTransaction.createUse(
            wallet.getWalletId(),
            earnedPointAmount,
            wallet.getBalance(),
            "주문 취소 - 적립 포인트 회수 (Order ID: " + orderId + ")"
        );
        savePointTransactionPort.save(pointTransaction);
    }
}