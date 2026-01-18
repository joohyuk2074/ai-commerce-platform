package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.application.dto.result.CartResult;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.entity.CartItem;
import com.spartaecommerce.cart.domain.port.in.GetCartUseCase;
import com.spartaecommerce.cart.domain.port.out.CartStoragePort;
import com.spartaecommerce.cart.domain.port.out.LoadCartPort;
import com.spartaecommerce.cart.application.dto.query.CartGetQuery;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.common.auth.Passport;
import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.pointwallet.domain.entity.PointPolicy;
import com.spartaecommerce.pointwallet.domain.service.PointCalculator;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.LoadProductPort;
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

    private final LoadCartPort loadCartPort;
    private final LoadProductPort loadProductPort;
    private final LoadCategoryPort loadCategoryPort;
    private final CartStoragePort cartStoragePort;
    private final PointsProperties pointsProperties;
    private final PointCalculator pointCalculator;

    @Override
    public CartResult get(CartGetQuery query) {
        Cart cart = getCartFromStorageOrDatabase(query.userId());

        if (cart.isEmpty()) {
            return CartResult.from(cart, BigDecimal.ZERO);
        }

        Passport passport = query.passport();
        Map<Long, Category> productIdToCategoryMap = buildProductToCategoryMap(cart);
        PointPolicy policy = createPointPolicy();

        BigDecimal expectedPoints = pointCalculator.calculateExpectedPoints(
            cart,
            passport,
            productIdToCategoryMap,
            policy
        );

        return CartResult.from(cart, expectedPoints);
    }

    private Cart getCartFromStorageOrDatabase(Long userId) {
        return cartStoragePort.get(userId)
            .orElseGet(() -> {
                Cart dbCart = loadCartPort.findByUserId(userId)
                    .orElseGet(() -> Cart.createNew(userId));

                if (dbCart.getCartId() != null) {
                    cartStoragePort.save(dbCart);
                }

                return dbCart;
            });
    }

    private Map<Long, Category> buildProductToCategoryMap(Cart cart) {
        List<Long> productIds = cart.getItems().stream()
            .map(CartItem::getProductId)
            .toList();

        List<Product> products = loadProductPort.findAllByProductIdIn(productIds);

        Set<Long> categoryIds = products.stream()
            .map(Product::getCategoryId)
            .collect(Collectors.toSet());

        List<Category> categories = loadCategoryPort.findAllByCategoryIdIn(categoryIds);

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
