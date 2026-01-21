package com.spartaecommerce.pointwallet.adapter.in.web.internal;

import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.util.DateTimeHolder;
import com.spartaecommerce.pointwallet.adapter.in.web.internal.dto.PointWalletDto;
import com.spartaecommerce.pointwallet.adapter.in.web.internal.dto.PointOperationRequest;
import com.spartaecommerce.pointwallet.adapter.in.web.internal.dto.PointCalculationRequest;
import com.spartaecommerce.pointwallet.adapter.in.web.internal.dto.PointCalculationResponse;
import com.spartaecommerce.common.domain.pointwallet.PointTransaction;
import com.spartaecommerce.common.domain.pointwallet.PointWallet;
import com.spartaecommerce.common.domain.pointwallet.PointPolicy;
import com.spartaecommerce.pointwallet.domain.port.out.LoadPointWalletPort;
import com.spartaecommerce.pointwallet.domain.port.out.SavePointTransactionPort;
import com.spartaecommerce.pointwallet.domain.port.out.SavePointWalletPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Internal API for inter-service communication
 */
@RestController
@RequestMapping("/internal/v1/point-wallets")
@RequiredArgsConstructor
public class PointWalletInternalController {

    private final LoadPointWalletPort loadPointWalletPort;
    private final SavePointWalletPort savePointWalletPort;
    private final SavePointTransactionPort savePointTransactionPort;
    private final PointsProperties pointsProperties;
    private final DateTimeHolder dateTimeHolder;

    /**
     * Get point wallet by user ID
     */
    @GetMapping("/users/{userId}")
    public CommonResponse<PointWalletDto> getPointWallet(@PathVariable Long userId) {
        PointWallet wallet = loadPointWalletPort.getByUserId(userId);
        return CommonResponse.success(PointWalletDto.from(wallet));
    }

    /**
     * Use points
     */
    @PostMapping("/users/{userId}/use")
    public CommonResponse<PointWalletDto> usePoints(
        @PathVariable Long userId,
        @RequestBody PointOperationRequest request
    ) {
        PointWallet wallet = loadPointWalletPort.getByUserId(userId);
        Money usePoints = Money.from(request.amount());

        wallet.usePoints(usePoints, pointsProperties.getMinUsagePoint());
        savePointWalletPort.save(wallet);

        // Record transaction
        PointTransaction transaction = PointTransaction.createUse(
            wallet.getWalletId(),
            usePoints,
            wallet.getBalance(),
            request.description()
        );
        savePointTransactionPort.save(transaction);

        return CommonResponse.success(PointWalletDto.from(wallet));
    }

    /**
     * Earn points
     */
    @PostMapping("/users/{userId}/earn")
    public CommonResponse<PointWalletDto> earnPoints(
        @PathVariable Long userId,
        @RequestBody PointOperationRequest request
    ) {
        PointWallet wallet = loadPointWalletPort.getByUserId(userId);
        Money earnPoints = Money.from(request.amount());
        Money maxBalance = Money.from(pointsProperties.getMaxBalance());

        wallet.earnPoints(earnPoints, maxBalance);
        savePointWalletPort.save(wallet);

        // Record transaction
        LocalDateTime expireAt = dateTimeHolder.getCurrentDateTime().plusDays(pointsProperties.getExpireDays());
        PointTransaction transaction = PointTransaction.createEarn(
            wallet.getWalletId(),
            earnPoints,
            wallet.getBalance(),
            expireAt,
            request.description()
        );
        savePointTransactionPort.save(transaction);

        return CommonResponse.success(PointWalletDto.from(wallet));
    }

    /**
     * Calculate expected points for order items
     */
    @PostMapping("/calculate-points")
    public CommonResponse<PointCalculationResponse> calculatePoints(
        @RequestBody PointCalculationRequest request
    ) {
        PointPolicy pointPolicy = new PointPolicy(
            pointsProperties.getDefaultRate(),
            pointsProperties.getVipRate(),
            pointsProperties.getCategoryRateMapAsBigDecimal(),
            BigDecimal.ZERO
        );

        BigDecimal totalPoints = BigDecimal.ZERO;

        for (PointCalculationRequest.OrderItemDto item : request.orderItems()) {
            BigDecimal effectiveRate = resolveEffectiveRate(request.userGrade(), item.categoryId(), pointPolicy);
            BigDecimal itemPoints = item.totalPrice().multiply(effectiveRate);
            totalPoints = totalPoints.add(itemPoints);
        }

        // Round down
        totalPoints = totalPoints.setScale(0, RoundingMode.DOWN);

        return CommonResponse.success(new PointCalculationResponse(totalPoints));
    }

    private BigDecimal resolveEffectiveRate(String userGrade, Long categoryId, PointPolicy policy) {
        boolean isVip = "VIP".equals(userGrade);
        BigDecimal userRate = isVip ? policy.vipRate() : policy.defaultRate();

        // For simplicity, using default rate. In real scenario, should lookup category rate by ID
        return userRate;
    }
}
