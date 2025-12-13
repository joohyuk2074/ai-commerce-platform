package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.application.dto.command.CartUpdateItemQuantityCommand;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.in.UpdateCartItemQuantityUseCase;
import com.spartaecommerce.cart.domain.port.out.CartStoragePort;
import com.spartaecommerce.cart.domain.port.out.LoadCartPort;
import com.spartaecommerce.cart.domain.port.out.SaveCartPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartItemUpdateService implements UpdateCartItemQuantityUseCase {

    private final LoadCartPort loadCartPort;
    private final SaveCartPort saveCartPort;
    private final CartStoragePort cartStoragePort;

    @Override
    public void updateItemQuantity(CartUpdateItemQuantityCommand command) {
        Cart cart = loadCartPort.getByUserId(command.userId());
        cart.updateItemQuantity(command.productId(), command.quantity());

        Cart updatedCart = saveCartPort.save(cart);
        cartStoragePort.save(updatedCart);
    }
}
