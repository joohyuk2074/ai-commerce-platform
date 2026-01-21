package com.spartaecommerce.common.domain.pointwallet;

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
