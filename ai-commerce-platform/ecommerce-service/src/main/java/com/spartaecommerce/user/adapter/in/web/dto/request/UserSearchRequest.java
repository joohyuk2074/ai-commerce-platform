package com.spartaecommerce.user.adapter.in.web.dto.request;

import com.spartaecommerce.common.domain.CustomPageable;
import com.spartaecommerce.user.application.dto.query.UserSearchQuery;

public record UserSearchRequest(
    String email,
    String name,

    Integer page,
    Integer size,
    String sortBy,
    String direction
) {
    public UserSearchQuery toQuery() {
        CustomPageable customPageable = CustomPageable.of(page, size, sortBy, direction, null);

        return new UserSearchQuery(
            email,
            name,
            customPageable
        );
    }
}
