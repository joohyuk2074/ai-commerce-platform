package com.spartaecommerce.cart.domain.port.in;

import com.spartaecommerce.cart.application.dto.command.CartRemoveItemCommand;

public interface RemoveCartItemUseCase {

    void removeItem(CartRemoveItemCommand command);
}
