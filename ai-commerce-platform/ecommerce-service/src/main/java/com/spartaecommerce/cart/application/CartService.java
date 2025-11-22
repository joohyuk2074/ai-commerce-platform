package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.application.dto.result.CartResult;
import com.spartaecommerce.cart.domain.command.CartAddItemCommand;
import com.spartaecommerce.cart.domain.command.CartClearCommand;
import com.spartaecommerce.cart.domain.command.CartRemoveItemCommand;
import com.spartaecommerce.cart.domain.command.CartUpdateItemQuantityCommand;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.entity.CartItem;
import com.spartaecommerce.cart.domain.query.CartGetQuery;
import com.spartaecommerce.cart.domain.repository.CartRepository;
import com.spartaecommerce.cart.domain.storage.CartStorage;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.repository.CategoryRepository;
import com.spartaecommerce.common.config.properties.CartProperties;
import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.pointwallet.domain.entity.PointPolicy;
import com.spartaecommerce.pointwallet.domain.service.PointCalculator;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductRepository;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.repository.UserRepository;
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
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartStorage cartStorage;
    private final PointsProperties pointsProperties;
    private final CartProperties cartProperties;
    private final PointCalculator pointCalculator;


    @Transactional
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

    @Transactional
    public void removeItem(CartRemoveItemCommand command) {
        Cart cart = cartRepository.getByUserId(command.userId());

        cart.removeItem(command.productId());

        Cart updatedCart = cartRepository.save(cart);
        cartStorage.save(updatedCart);
    }

    @Transactional
    public void updateItemQuantity(CartUpdateItemQuantityCommand command) {
        Cart cart = cartRepository.getByUserId(command.userId());
        cart.updateItemQuantity(command.productId(), command.quantity());

        Cart updatedCart = cartRepository.save(cart);
        cartStorage.save(updatedCart);
    }

    @Transactional
    public void clear(CartClearCommand command) {
        Cart cart = cartRepository.getByUserId(command.userId());
        cart.clear();

        Cart clearedCart = cartRepository.save(cart);
        cartStorage.delete(clearedCart.getUserId());
    }

    public CartResult get(CartGetQuery query) {
        Cart cart = getCartFromStorageOrDatabase(query.userId());

        if (cart.isEmpty()) {
            return CartResult.from(cart, BigDecimal.ZERO);
        }

        User user = userRepository.getById(query.userId());
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