package com.spartaecommerce.common.domain;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
    List<T> contents,
    Integer page,
    Integer size,
    Long totalElements,
    Integer totalPages
) {
    public static <T> PageResponse<T> of(List<T> contents) {
        return new PageResponse<>(contents, null, null, null, null);
    }

    public static <T> PageResponse<T> of(Page<T> orderResponses) {
        return new PageResponse<>(
            orderResponses.getContent(),
            orderResponses.getNumber(),
            orderResponses.getSize(),
            orderResponses.getTotalElements(),
            orderResponses.getTotalPages()
        );
    }
}
