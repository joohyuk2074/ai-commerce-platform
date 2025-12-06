package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.application.dto.result.CartResult;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.entity.CartItem;
import com.spartaecommerce.cart.domain.port.in.GetCartUseCase;
import com.spartaecommerce.cart.domain.query.CartGetQuery;
import com.spartaecommerce.cart.domain.repository.CartRepository;
import com.spartaecommerce.cart.domain.storage.CartStorage;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.repository.CategoryRepository;
import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.pointwallet.domain.entity.PointPolicy;
import com.spartaecommerce.pointwallet.domain.service.PointCalculator;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartQueryService implements GetCartUseCase {

    private final LoadUserPort loadUserPort;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartStorage cartStorage;
    private final PointsProperties pointsProperties;
    private final PointCalculator pointCalculator;

    @Override
    public CartResult get(CartGetQuery query) {
        Cart cart = getCartFromStorageOrDatabase(query.userId());

        if (cart.isEmpty()) {
            return CartResult.from(cart, BigDecimal.ZERO);
        }

        User user = loadUserPort.getById(query.userId());
        Map<Long, Category> productIdToCategoryMap = buildProductToCategoryMap(cart);
        PointPolicy policy = createPointPolicy();

        BigDecimal expectedPoints = pointCalculator.calculateExpectedPoints(
            cart,
            user,
            productIdToCategoryMap,
            policy
        );

        return CartResult.from(cart, expectedPoints);
    }

    private Cart getCartFromStorageOrDatabase(Long userId) {
        return cartStorage.get(userId)
            .orElseGet(() -> {
                Cart dbCart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> Cart.createNew(userId));

                if (dbCart.getCartId() != null) {
                    cartStorage.save(dbCart);
                }

                return dbCart;
            });
    }

    private Map<Long, Category> buildProductToCategoryMap(Cart cart) {
        List<Long> productIds = cart.getItems().stream()
            .map(CartItem::getProductId)
            .toList();

        List<Product> products = productRepository.findAllByProductIdIn(productIds);

        Set<Long> categoryIds = products.stream()
            .map(Product::getCategoryId)
            .collect(Collectors.toSet());

        List<Category> categories = categoryRepository.findAllByCategoryIdIn(categoryIds);

        Map<Long, Category> categoryMap = categories.stream()
            .collect(Collectors.toMap(
                Category::getCategoryId,
                Function.identity()
            ));

        return products.stream()
            .filter(product -> categoryMap.containsKey(product.getCategoryId()))
            .collect(Collectors.toMap(
                Product::getProductId,
                product -> categoryMap.get(product.getCategoryId())
            ));
    }

    private PointPolicy createPointPolicy() {
        return new PointPolicy(
            pointsProperties.getDefaultRate(),
            pointsProperties.getVipRate(),
            pointsProperties.getCategoryRateMapAsBigDecimal(),
            BigDecimal.ZERO
        );
    }
}
