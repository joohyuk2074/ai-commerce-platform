package com.spartaecommerce.cart.application;

import com.spartaecommerce.cart.application.dto.result.CartResult;
import com.spartaecommerce.cart.domain.command.CartAddItemCommand;
import com.spartaecommerce.cart.domain.query.CartGetQuery;
import com.spartaecommerce.cart.domain.repository.CartFakeRepository;
import com.spartaecommerce.cart.domain.storage.CartFakeStorage;
import com.spartaecommerce.cart.domain.storage.CartStorage;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.repository.CategoryFakeRepository;
import com.spartaecommerce.common.config.properties.CartProperties;
import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.pointwallet.domain.service.PointCalculator;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.repository.ProductFakeRepository;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.repository.UserFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CartService")
class CartServiceTest {

    private CartService sut;
    private UserFakeRepository userRepository;
    private CartFakeRepository cartRepository;
    private ProductFakeRepository productRepository;
    private CategoryFakeRepository categoryRepository;
    private PointsProperties pointsProperties;
    private CartProperties cartProperties;
    private CartStorage cartStorage;
    private PointCalculator pointCalculator;

    @BeforeEach
    void setUp() {
        userRepository = new UserFakeRepository();
        cartRepository = new CartFakeRepository();
        productRepository = new ProductFakeRepository();
        categoryRepository = new CategoryFakeRepository();
        cartStorage = new CartFakeStorage();
        pointCalculator = new PointCalculator();

        // PointsProperties 설정
        pointsProperties = new PointsProperties();
        pointsProperties.setDefaultRate(new BigDecimal("0.01"));
        pointsProperties.setVipRate(new BigDecimal("0.03"));
        pointsProperties.setCategoryRateMap(new HashMap<>() {{
            put("electronics", "0.05");
            put("clothing", "0.03");
            put("food", "0.02");
            put("book", "0.10");
        }});
        pointsProperties.setMinUsageAmount(10000);
        pointsProperties.setMinUsagePoint(1000);
        pointsProperties.setExpireDays(30);
        pointsProperties.setMaxBalance(BigDecimal.valueOf(1000000L));

        // CartProperties 설정
        cartProperties = new CartProperties();
        cartProperties.setMaxItems(20);

        sut = new CartService(
            userRepository,
            cartRepository,
            productRepository,
            categoryRepository,
            cartStorage,
            pointsProperties,
            cartProperties,
            pointCalculator
        );
    }

    @AfterEach
    void tearDown() {
        userRepository.clear();
        cartRepository.clear();
        productRepository.clear();
        categoryRepository.clear();
    }

    @Test
    @DisplayName("카테고리별 가중치가 반영된 예상 포인트 계산 - 전자제품 5%")
    void calculateExpectedPoints_WithElectronicsCategoryWeight() {
        // given
        User user = User.createNew("test@test.com", "테스트", "010-1234-5678");
        Long userId = userRepository.save(user);

        // 전자제품 카테고리 생성 (5% 포인트 적립)
        Category category = Category.createNew("전자제품", "전자제품 카테고리", null);
        Long categoryId = categoryRepository.save(category);

        // 상품 생성 (가격: 10,000원, 재고: 100개)
        Product product = Product.createNew(
            "노트북",
            "고성능 노트북",
            Money.from(new BigDecimal("10000")),
            100,
            categoryId
        );
        Long productId = productRepository.save(product);

        // when
        CartAddItemCommand addCommand = new CartAddItemCommand(userId, productId, 2);
        sut.addItem(addCommand);

        CartResult result = sut.get(new CartGetQuery(userId));

        // then
        // 총 금액: 10,000 * 2 = 20,000
        // 예상 포인트: 20,000 * 0.05 = 1,000
        assertThat(result.totalAmount().amount()).isEqualByComparingTo(new BigDecimal("20000.00"));
        assertThat(result.expectedPoints()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("카테고리별 가중치가 반영된 예상 포인트 계산 - 의류 3%")
    void calculateExpectedPoints_WithClothingCategoryWeight() {
        // given
        User user = User.createNew("test@test.com", "테스트", "010-1234-5678");
        Long userId = userRepository.save(user);

        // 의류 카테고리 생성 (3% 포인트 적립)
        Category category = Category.createNew("의류", "의류 카테고리", null);
        Long categoryId = categoryRepository.save(category);

        // 상품 생성 (가격: 50,000원, 재고: 50개)
        Product product = Product.createNew(
            "티셔츠",
            "면 100% 티셔츠",
            Money.from(new BigDecimal("50000")),
            50,
            categoryId
        );
        Long productId = productRepository.save(product);

        // when
        CartAddItemCommand addCommand = new CartAddItemCommand(userId, productId, 1);
        sut.addItem(addCommand);

        CartResult result = sut.get(new CartGetQuery(userId));

        // then
        // 총 금액: 50,000 * 1 = 50,000
        // 예상 포인트: 50,000 * 0.03 = 1,500
        assertThat(result.totalAmount().amount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(result.expectedPoints()).isEqualByComparingTo(new BigDecimal("1500"));
    }

    @Test
    @DisplayName("카테고리별 가중치가 반영된 예상 포인트 계산 - 도서 10%")
    void calculateExpectedPoints_WithBookCategoryWeight() {
        // given
        User user = User.createNew("test@test.com", "테스트", "010-1234-5678");
        Long userId = userRepository.save(user);

        // 도서 카테고리 생성 (10% 포인트 적립)
        Category category = Category.createNew("도서", "도서 카테고리", null);
        Long categoryId = categoryRepository.save(category);

        // 상품 생성 (가격: 15,000원, 재고: 100개)
        Product product = Product.createNew(
            "클린 코드",
            "로버트 C. 마틴",
            Money.from(new BigDecimal("15000")),
            100,
            categoryId
        );
        Long productId = productRepository.save(product);

        // when
        CartAddItemCommand addCommand = new CartAddItemCommand(userId, productId, 3);
        sut.addItem(addCommand);

        CartResult result = sut.get(new CartGetQuery(userId));

        // then
        // 총 금액: 15,000 * 3 = 45,000
        // 예상 포인트: 45,000 * 0.10 = 4,500
        assertThat(result.totalAmount().amount()).isEqualByComparingTo(new BigDecimal("45000.00"));
        assertThat(result.expectedPoints()).isEqualByComparingTo(new BigDecimal("4500"));
    }

    @Test
    @DisplayName("여러 카테고리 상품이 섞인 장바구니의 예상 포인트 계산")
    void calculateExpectedPoints_WithMultipleCategoryProducts() {
        // given
        User user = User.createNew("test@test.com", "테스트", "010-1234-5678");
        Long userId = userRepository.save(user);

        // 전자제품 카테고리 (5%)
        Category electronicsCategory = Category.createNew("전자제품", "전자제품 카테고리", null);
        Long electronicsCategoryId = categoryRepository.save(electronicsCategory);

        // 도서 카테고리 (10%)
        Category bookCategory = Category.createNew("도서", "도서 카테고리", null);
        Long bookCategoryId = categoryRepository.save(bookCategory);

        // 전자제품 상품 (10,000원)
        Product electronicsProduct = Product.createNew(
            "마우스",
            "무선 마우스",
            Money.from(new BigDecimal("10000")),
            100,
            electronicsCategoryId
        );
        Long electronicsProductId = productRepository.save(electronicsProduct);

        // 도서 상품 (20,000원)
        Product bookProduct = Product.createNew(
            "이펙티브 자바",
            "조슈아 블로크",
            Money.from(new BigDecimal("20000")),
            100,
            bookCategoryId
        );
        Long bookProductId = productRepository.save(bookProduct);

        // when
        sut.addItem(new CartAddItemCommand(userId, electronicsProductId, 2)); // 20,000원
        sut.addItem(new CartAddItemCommand(userId, bookProductId, 1));         // 20,000원

        CartResult result = sut.get(new CartGetQuery(userId));

        // then
        // 총 금액: 20,000 + 20,000 = 40,000
        // 예상 포인트: (20,000 * 0.05) + (20,000 * 0.10) = 1,000 + 2,000 = 3,000
        assertThat(result.totalAmount().amount()).isEqualByComparingTo(new BigDecimal("40000.00"));
        assertThat(result.expectedPoints()).isEqualByComparingTo(new BigDecimal("3000"));
    }

    @Test
    @DisplayName("최대 품목 수(20개) 초과 시 BusinessException 발생")
    void addItem_ExceedMaxItems_ThrowsBusinessException() {
        // given
        User user = User.createNew("test@test.com", "테스트", "010-1234-5678");
        Long userId = userRepository.save(user);

        Category category = Category.createNew("전자제품", "전자제품 카테고리", null);
        Long categoryId = categoryRepository.save(category);

        // 20개의 상품을 장바구니에 추가
        for (int i = 1; i <= 20; i++) {
            Product product = Product.createNew(
                "상품" + i,
                "설명" + i,
                Money.from(new BigDecimal("1000")),
                100,
                categoryId
            );
            Long productId = productRepository.save(product);
            sut.addItem(new CartAddItemCommand(userId, productId, 1));
        }

        // 21번째 상품 추가 시도
        Product extraProduct = Product.createNew(
            "상품21",
            "설명21",
            Money.from(new BigDecimal("1000")),
            100,
            categoryId
        );
        Long extraProductId = productRepository.save(extraProduct);

        // when & then
        assertThatThrownBy(() -> sut.addItem(new CartAddItemCommand(userId, extraProductId, 1)))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }
}