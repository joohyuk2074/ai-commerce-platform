package com.spartaecommerce.pointwallet.domain.entity;

import java.math.BigDecimal;
import java.util.Map;

public record PointPolicy(
    BigDecimal defaultRate,
    BigDecimal vipRate,
    Map<String, BigDecimal> categoryRateMap,
    BigDecimal couponMultiplier
) {

    public BigDecimal categoryRateOrDefault(String categoryName, BigDecimal fallback) {
        return categoryRateMap.getOrDefault(categoryName, fallback);
    }
}
