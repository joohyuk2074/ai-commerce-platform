package com.spartaecommerce.cart.application.dto.query;

import com.spartaecommerce.common.auth.Passport;

public record CartGetQuery(
    Long userId,
    Passport passport
) {
}