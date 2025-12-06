package com.spartaecommerce.cart.domain.port.out;

import com.spartaecommerce.cart.domain.entity.Cart;

public interface SaveCartPort {

    Cart save(Cart cart);

    void deleteByUserId(Long userId);
}
