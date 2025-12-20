package com.spartaecommerce.product.application.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalProductMessage {

    @JsonProperty("contents")
    private List<ExternalProductData> contents;

    @JsonProperty("pageable")
    private ExternalProductPageable pageable;
}
