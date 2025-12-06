package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.domain.command.CartClearCommand;
import com.spartaecommerce.cart.domain.command.CartRemoveItemCommand;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.in.ClearCartUseCase;
import com.spartaecommerce.cart.domain.port.in.RemoveCartItemUseCase;
import com.spartaecommerce.cart.domain.repository.CartRepository;
import com.spartaecommerce.cart.domain.storage.CartStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartItemRemoveService implements RemoveCartItemUseCase, ClearCartUseCase {

    private final CartRepository cartRepository;
    private final CartStorage cartStorage;

    @Override
    public void removeItem(CartRemoveItemCommand command) {
        Cart cart = cartRepository.getByUserId(command.userId());

        cart.removeItem(command.productId());

        Cart updatedCart = cartRepository.save(cart);
        cartStorage.save(updatedCart);
    }

    @Override
    public void clear(CartClearCommand command) {
        Cart cart = cartRepository.getByUserId(command.userId());
        cart.clear();

        Cart clearedCart = cartRepository.save(cart);
        cartStorage.delete(clearedCart.getUserId());
    }
}
