package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.domain.command.CartAddItemCommand;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.in.AddCartItemUseCase;
import com.spartaecommerce.cart.domain.repository.CartRepository;
import com.spartaecommerce.cart.domain.storage.CartStorage;
import com.spartaecommerce.common.config.properties.CartProperties;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemAddService implements AddCartItemUseCase {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartProperties cartProperties;
    private final CartStorage cartStorage;

    @Override
    public void addItem(CartAddItemCommand command) {
        Product product = productRepository.getById(command.productId());

        Cart cart = cartRepository.findByUserId(command.userId())
            .orElseGet(() -> Cart.createNew(command.userId()));

        cart.addItem(
            command.productId(),
            command.quantity(),
            product.getPrice(),
            cartProperties.getMaxItems()
        );

        Cart savedCart = cartRepository.save(cart);
        cartStorage.save(savedCart);
    }
}
