package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.application.dto.command.CartAddItemCommand;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.in.AddCartItemUseCase;
import com.spartaecommerce.cart.domain.port.out.CartStoragePort;
import com.spartaecommerce.cart.domain.port.out.LoadCartPort;
import com.spartaecommerce.cart.domain.port.out.SaveCartPort;
import com.spartaecommerce.common.config.properties.CartProperties;
import com.spartaecommerce.common.domain.product.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemAddService implements AddCartItemUseCase {

    private final LoadProductPort loadProductPort;
    private final LoadCartPort loadCartPort;
    private final SaveCartPort saveCartPort;
    private final CartProperties cartProperties;
    private final CartStoragePort cartStoragePort;

    @Override
    public void addItem(CartAddItemCommand command) {
        Product product = loadProductPort.getById(command.productId());

        Cart cart = loadCartPort.findByUserId(command.userId())
            .orElseGet(() -> Cart.createNew(command.userId()));

        cart.addItem(
            command.productId(),
            command.quantity(),
            product.getPrice(),
            cartProperties.getMaxItems()
        );

        Cart savedCart = saveCartPort.save(cart);
        cartStoragePort.save(savedCart);
    }
}
