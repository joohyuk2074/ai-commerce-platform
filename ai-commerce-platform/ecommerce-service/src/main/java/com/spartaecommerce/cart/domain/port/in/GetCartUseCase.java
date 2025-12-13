package com.spartaecommerce.cart.domain.port.in;

import com.spartaecommerce.cart.application.dto.result.CartResult;
import com.spartaecommerce.cart.application.dto.query.CartGetQuery;

public interface GetCartUseCase {

    CartResult get(CartGetQuery query);
}
