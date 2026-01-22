package com.spartaecommerce.product.application.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalProductApiResponse {

    @JsonProperty("result")
    private Boolean result;

    @JsonProperty("message")
    private ExternalProductMessage message;

    @JsonProperty("error")
    private ExternalProductError error;
}
