package com.spartaecommerce.cart.domain.port.in;

import com.spartaecommerce.cart.application.dto.command.CartClearCommand;

public interface ClearCartUseCase {

    void clear(CartClearCommand command);
}
