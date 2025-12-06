package com.spartaecommerce.cart.domain.port.in;

import com.spartaecommerce.cart.domain.command.CartAddItemCommand;

public interface AddCartItemUseCase {

    void addItem(CartAddItemCommand command);
}
