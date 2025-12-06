package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.domain.command.CartUpdateItemQuantityCommand;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.in.UpdateCartItemQuantityUseCase;
import com.spartaecommerce.cart.domain.repository.CartRepository;
import com.spartaecommerce.cart.domain.storage.CartStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartItemUpdateService implements UpdateCartItemQuantityUseCase {

    private final CartRepository cartRepository;
    private final CartStorage cartStorage;

    @Override
    public void updateItemQuantity(CartUpdateItemQuantityCommand command) {
        Cart cart = cartRepository.getByUserId(command.userId());
        cart.updateItemQuantity(command.productId(), command.quantity());

        Cart updatedCart = cartRepository.save(cart);
        cartStorage.save(updatedCart);
    }
}
