package com.spartaecommerce.pointwallet.domain.service;

import com.spartaecommerce.common.domain.category.Category;
import com.spartaecommerce.common.auth.Passport;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.domain.pointwallet.PointPolicy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class PointCalculator {

    /**
     * MSA: Order와 Cart 의존성 제거 - 범용 DTO 사용
     */
    public BigDecimal calculateExpectedPoints(
        List<ItemForPointCalculation> items,
        Passport passport,
        Map<Long, Category> categoryResolver,
        PointPolicy policy
    ) {
        BigDecimal totalPoints = BigDecimal.ZERO;

        for (ItemForPointCalculation item : items) {
            Category category = categoryResolver.get(item.productId());

            if (category == null) {
                continue;
            }

            BigDecimal effectiveRate = resolveEffectiveRate(passport, category, policy);

            BigDecimal itemPoints = item.totalPrice()
                .amount()
                .multiply(effectiveRate);

            totalPoints = totalPoints.add(itemPoints);
        }

        // 소수점 버림
        return totalPoints.setScale(0, RoundingMode.DOWN);
    }

    /**
     * DTO for point calculation (replaces OrderItem and CartItem dependencies)
     */
    public record ItemForPointCalculation(
        Long productId,
        Money totalPrice
    ) {
    }

    private BigDecimal resolveEffectiveRate(
        Passport passport,
        Category category,
        PointPolicy policy
    ) {
        // Passport의 grade를 기반으로 VIP 여부 판단
        boolean isVip = "VIP".equals(passport.grade());

        BigDecimal userRate = isVip
            ? policy.vipRate()
            : policy.defaultRate();

        BigDecimal categoryBaseRate = policy.categoryRateOrDefault(
            category.getName(),
            userRate
        );

        return categoryBaseRate.max(userRate);
    }
}
