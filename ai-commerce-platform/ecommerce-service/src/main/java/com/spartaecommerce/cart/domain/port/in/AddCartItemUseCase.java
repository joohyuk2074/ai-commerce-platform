package com.spartaecommerce.cart.domain.port.in;

import com.spartaecommerce.cart.application.dto.command.CartAddItemCommand;

public interface AddCartItemUseCase {

    void addItem(CartAddItemCommand command);
}
