package com.spartaecommerce.cart.domain.port.in;

import com.spartaecommerce.cart.application.dto.command.CartUpdateItemQuantityCommand;

public interface UpdateCartItemQuantityUseCase {

    void updateItemQuantity(CartUpdateItemQuantityCommand command);
}
