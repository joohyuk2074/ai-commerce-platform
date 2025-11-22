package com.spartaecommerce.pointwallet.domain.service;

import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.entity.CartItem;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.pointwallet.domain.entity.PointPolicy;
import com.spartaecommerce.user.domain.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class PointCalculator {

    public BigDecimal calculateExpectedPoints(
        List<OrderItem> orderItems,
        User user,
        Map<Long, Category> categoryResolver,
        PointPolicy policy
    ) {
        BigDecimal totalPoints = BigDecimal.ZERO;

        for (OrderItem item : orderItems) {
            Category category = categoryResolver.get(item.getProductId());

            if (category == null) {
                return totalPoints;
            }

            BigDecimal effectiveRate = resolveEffectiveRate(user, category, policy);

            BigDecimal itemPoints = item.getTotalPrice()
                .amount()
                .multiply(effectiveRate);

            totalPoints = totalPoints.add(itemPoints);
        }

//        // 쿠폰 배수까지 적용
//        totalPoints = totalPoints.multiply(policy.couponMultiplier());

        // 소수점 버림
        return totalPoints.setScale(0, RoundingMode.DOWN);
    }

    public BigDecimal calculateExpectedPoints(
        Cart cart,
        User user,
        Map<Long, Category> categoryResolver,
        PointPolicy policy
    ) {
        BigDecimal totalPoints = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Category category = categoryResolver.get(item.getProductId());

            BigDecimal effectiveRate = resolveEffectiveRate(user, category, policy);

            BigDecimal itemPoints = item.getTotalPrice()
                .amount()
                .multiply(effectiveRate);

            totalPoints = totalPoints.add(itemPoints);
        }

//        // 쿠폰 배수까지 적용
//        totalPoints = totalPoints.multiply(policy.couponMultiplier());

        // 소수점 버림
        return totalPoints.setScale(0, RoundingMode.DOWN);
    }

    private BigDecimal resolveEffectiveRate(User user, Category category, PointPolicy policy) {
        BigDecimal userRate = user.isVip()
            ? policy.vipRate()
            : policy.defaultRate();

        BigDecimal categoryBaseRate = policy.categoryRateOrDefault(
            category.getName(),
            userRate
        );

        return categoryBaseRate.max(userRate);
    }
}
