package com.spartaecommerce.cart.domain.port.in;

import com.spartaecommerce.cart.domain.command.CartClearCommand;

public interface ClearCartUseCase {

    void clear(CartClearCommand command);
}
