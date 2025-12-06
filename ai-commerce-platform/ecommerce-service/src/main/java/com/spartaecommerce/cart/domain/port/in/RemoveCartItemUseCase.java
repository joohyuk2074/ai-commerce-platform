package com.spartaecommerce.cart.domain.port.in;

import com.spartaecommerce.cart.domain.command.CartRemoveItemCommand;

public interface RemoveCartItemUseCase {

    void removeItem(CartRemoveItemCommand command);
}
