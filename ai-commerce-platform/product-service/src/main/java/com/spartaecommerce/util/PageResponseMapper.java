package com.spartaecommerce.util;

import com.spartaecommerce.common.domain.PageResponse;
import org.springframework.data.domain.Page;

public class PageResponseMapper {

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.of(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    private PageResponseMapper() {
        // Utility class
    }
}
