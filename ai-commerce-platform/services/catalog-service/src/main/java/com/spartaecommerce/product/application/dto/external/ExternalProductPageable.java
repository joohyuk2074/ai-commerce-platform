package com.spartaecommerce.product.application.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalProductPageable {

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("pageNumber")
    private Integer pageNumber;

    @JsonProperty("pageSize")
    private Integer pageSize;

    @JsonProperty("pageElements")
    private Integer pageElements;

    @JsonProperty("totalElements")
    private Integer totalElements;

    @JsonProperty("totalPages")
    private Integer totalPages;

    @JsonProperty("first")
    private Boolean first;

    @JsonProperty("last")
    private Boolean last;
}
