package com.spartaecommerce.common.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "cart")
public class CartProperties {

    /**
     * 장바구니에 담을 수 있는 최대 품목 수
     */
    @NotNull
    @Min(1)
    private Integer maxItems;
}