package com.spartaecommerce.user.domain.port.out;

import com.spartaecommerce.user.domain.entity.User;

public interface SaveUserPort {

    Long save(User user);
}
