package com.spartaecommerce.common.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "points")
public class PointsProperties {

    /**
     * 기본 적립률 (예: 0.01 = 1%)
     */
    @NotNull
    @Min(0)
    @Max(1)
    private BigDecimal defaultRate;

    /**
     * VIP 등급 사용자 전용 적립률
     */
    @NotNull
    @Min(0)
    @Max(1)
    private BigDecimal vipRate;

    /**
     * 카테고리별 가중치 (카테고리명 -> 적립률 문자열)
     * 예: { "전자제품": "0.05", "도서": "0.10" }
     */
    @NotNull
    private Map<String, String> categoryRateMap = new HashMap<>();

    /**
     * 포인트 사용이 가능한 주문 최소 금액
     */
    @NotNull
    @Min(0)
    private Integer minUsageAmount;

    /**
     * 한 번에 사용할 수 있는 최소 포인트 단위 (예: 1000P)
     */
    @NotNull
    @Min(0)
    private Integer minUsagePoint;

    /**
     * 적립 포인트 만료까지의 일수
     */
    @NotNull
    @Min(1)
    private Integer expireDays;

    /**
     * 포인트 지갑의 상한선
     */
    @NotNull
    @Min(0)
    private BigDecimal maxBalance;

    /**
     * 한글 카테고리명을 영문 키로 변환
     */
    private String getCategoryKey(String categoryName) {
        // 한글 -> 영문 매핑
        return switch (categoryName) {
            case "전자제품" -> "electronics";
            case "의류" -> "clothing";
            case "식품" -> "food";
            case "도서" -> "book";
            default -> categoryName.toLowerCase(); // 그 외는 소문자로 변환
        };
    }

    /**
     * 카테고리별 적립률 조회 (없으면 기본 적립률 반환)
     */
    public BigDecimal getCategoryRate(String categoryName) {
        String key = getCategoryKey(categoryName);
        String rateStr = categoryRateMap.get(key);
        if (rateStr == null) {
            return defaultRate;
        }
        return new BigDecimal(rateStr);
    }

    /**
     * 카테고리별 적립률 Map을 BigDecimal로 변환하여 반환
     */
    public Map<String, BigDecimal> getCategoryRateMapAsBigDecimal() {
        return categoryRateMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new BigDecimal(entry.getValue())
            ));
    }
}