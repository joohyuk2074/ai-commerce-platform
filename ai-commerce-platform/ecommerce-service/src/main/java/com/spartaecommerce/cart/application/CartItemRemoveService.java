package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.application.dto.command.CartClearCommand;
import com.spartaecommerce.cart.application.dto.command.CartRemoveItemCommand;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.in.ClearCartUseCase;
import com.spartaecommerce.cart.domain.port.in.RemoveCartItemUseCase;
import com.spartaecommerce.cart.domain.port.out.CartStoragePort;
import com.spartaecommerce.cart.domain.port.out.LoadCartPort;
import com.spartaecommerce.cart.domain.port.out.SaveCartPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartItemRemoveService implements RemoveCartItemUseCase, ClearCartUseCase {

    private final LoadCartPort loadCartPort;
    private final SaveCartPort saveCartPort;
    private final CartStoragePort cartStoragePort;

    @Override
    public void removeItem(CartRemoveItemCommand command) {
        Cart cart = loadCartPort.getByUserId(command.userId());

        cart.removeItem(command.productId());

        Cart updatedCart = saveCartPort.save(cart);
        cartStoragePort.save(updatedCart);
    }

    @Override
    public void clear(CartClearCommand command) {
        Cart cart = loadCartPort.getByUserId(command.userId());
        cart.clear();

        Cart clearedCart = saveCartPort.save(cart);
        cartStoragePort.delete(clearedCart.getUserId());
    }
}
