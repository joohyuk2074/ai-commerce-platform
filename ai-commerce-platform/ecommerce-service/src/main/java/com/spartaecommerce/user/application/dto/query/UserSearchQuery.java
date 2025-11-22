package com.spartaecommerce.user.application.dto.query;

import com.spartaecommerce.common.domain.CustomPageable;

public record UserSearchQuery(
    String email,
    String name,
    CustomPageable pageable
) {
}