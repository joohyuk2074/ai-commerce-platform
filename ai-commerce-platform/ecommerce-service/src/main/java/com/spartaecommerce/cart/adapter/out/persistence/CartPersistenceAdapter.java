package com.spartaecommerce.cart.adapter.out.persistence;

import com.spartaecommerce.cart.adapter.out.persistence.jpa.entity.CartJpaEntity;
import com.spartaecommerce.cart.adapter.out.persistence.jpa.repository.CartJpaRepository;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.out.LoadCartPort;
import com.spartaecommerce.cart.domain.port.out.SaveCartPort;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartPersistenceAdapter implements LoadCartPort, SaveCartPort {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public Cart save(Cart cart) {
        CartJpaEntity cartJpaEntity = CartJpaEntity.from(cart);
        return cartJpaRepository.save(cartJpaEntity).toDomain();
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        return cartJpaRepository.findByUserId(userId)
            .map(CartJpaEntity::toDomain);
    }

    @Override
    public Cart getByUserId(Long userId) {
        CartJpaEntity entity = cartJpaRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Cart not found for userId: " + userId
            ));

        return entity.toDomain();
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return cartJpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        cartJpaRepository.deleteByUserId(userId);
    }
}
