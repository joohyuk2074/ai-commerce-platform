package com.spartaecommerce.cart.domain.port.out;

import com.spartaecommerce.user.domain.entity.User;

public interface LoadUserPort {

    User getById(Long userId);
}
