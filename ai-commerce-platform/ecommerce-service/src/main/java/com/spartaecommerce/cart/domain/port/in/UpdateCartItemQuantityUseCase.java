package com.spartaecommerce.cart.domain.port.in;

import com.spartaecommerce.cart.domain.command.CartUpdateItemQuantityCommand;

public interface UpdateCartItemQuantityUseCase {

    void updateItemQuantity(CartUpdateItemQuantityCommand command);
}
