package com.spartaecommerce.pointwallet.domain.service;

import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.pointwallet.domain.entity.PointPolicy;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.entity.UserGrade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PointWalletCalculator 도메인 서비스")
class PointCalculatorTest {

    private PointCalculator calculator;
    private PointPolicy standardPolicy;

    @BeforeEach
    void setUp() {
        calculator = new PointCalculator();

        // 표준 정책 설정
        Map<String, BigDecimal> categoryRateMap = new HashMap<>();
        categoryRateMap.put("전자제품", new BigDecimal("0.05"));  // 5%
        categoryRateMap.put("의류", new BigDecimal("0.03"));      // 3%
        categoryRateMap.put("식품", new BigDecimal("0.02"));      // 2%
        categoryRateMap.put("도서", new BigDecimal("0.10"));      // 10%

        standardPolicy = new PointPolicy(
            new BigDecimal("0.01"),  // 일반 1%
            new BigDecimal("0.03"),  // VIP 3%
            categoryRateMap,
            BigDecimal.ZERO
        );
    }

    @ParameterizedTest
    @MethodSource("provideSingleProductScenarios")
    @DisplayName("단일 상품 포인트 계산 - 사용자 등급 및 카테고리별")
    void calculateExpectedPoints_SingleProduct(
        String scenario,
        UserGrade grade,
        String categoryName,
        String price,
        int quantity,
        String expectedPoints
    ) {
        // given
        User user = createUser(1L, grade);
        Category category = createCategory(1L, categoryName);
        Cart cart = createCart(1L);

        // 장바구니에 상품 추가
        addItemToCart(cart, 1L, new BigDecimal(price), quantity);

        Map<Long, Category> categoryMap = Map.of(1L, category);

        // when
        BigDecimal actualPoints = calculator.calculateExpectedPoints(
            cart, user, categoryMap, standardPolicy
        );

        // then
        assertThat(actualPoints).isEqualByComparingTo(new BigDecimal(expectedPoints));
    }

    static Stream<Arguments> provideSingleProductScenarios() {
        return Stream.of(
            // 일반 사용자
            Arguments.of("일반 사용자 - 일반 카테고리", UserGrade.NORMAL, "가구", "100000", 1, "1000"),
            Arguments.of("일반 사용자 - 전자제품", UserGrade.NORMAL, "전자제품", "100000", 1, "5000"),
            Arguments.of("일반 사용자 - 의류", UserGrade.NORMAL, "의류", "50000", 1, "1500"),
            Arguments.of("일반 사용자 - 식품", UserGrade.NORMAL, "식품", "20000", 1, "400"),
            Arguments.of("일반 사용자 - 도서", UserGrade.NORMAL, "도서", "30000", 1, "3000"),

            // VIP 사용자 (카테고리 vs VIP 중 큰 값 선택)
            Arguments.of("VIP - 일반 카테고리 (VIP 3% 적용)", UserGrade.VIP, "가구", "100000", 1, "3000"),
            Arguments.of("VIP - 전자제품 (카테고리 5% 적용)", UserGrade.VIP, "전자제품", "100000", 1, "5000"),
            Arguments.of("VIP - 의류 (VIP 3% 적용)", UserGrade.VIP, "의류", "100000", 1, "3000"),
            Arguments.of("VIP - 도서 (카테고리 10% 적용)", UserGrade.VIP, "도서", "50000", 1, "5000"),

            // 수량 테스트
            Arguments.of("일반 사용자 - 전자제품 x2", UserGrade.NORMAL, "전자제품", "50000", 2, "5000"),
            Arguments.of("VIP - 도서 x3", UserGrade.VIP, "도서", "15000", 3, "4500")
        );
    }

    @Test
    @DisplayName("여러 카테고리 상품 조합 - 일반 사용자")
    void calculateExpectedPoints_MultipleProducts_NormalUser() {
        // given
        User user = createUser(1L, UserGrade.NORMAL);
        Cart cart = createCart(1L);

        // 전자제품: 100,000원 x 1개 = 100,000원 → 5,000 포인트
        addItemToCart(cart, 1L, new BigDecimal("100000"), 1);

        // 의류: 50,000원 x 2개 = 100,000원 → 3,000 포인트
        addItemToCart(cart, 2L, new BigDecimal("50000"), 2);

        // 도서: 20,000원 x 1개 = 20,000원 → 2,000 포인트
        addItemToCart(cart, 3L, new BigDecimal("20000"), 1);

        Map<Long, Category> categoryMap = Map.of(
            1L, createCategory(1L, "전자제품"),
            2L, createCategory(2L, "의류"),
            3L, createCategory(3L, "도서")
        );

        // when
        BigDecimal actualPoints = calculator.calculateExpectedPoints(
            cart, user, categoryMap, standardPolicy
        );

        // then: 5,000 + 3,000 + 2,000 = 10,000
        assertThat(actualPoints).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("여러 카테고리 상품 조합 - VIP 사용자")
    void calculateExpectedPoints_MultipleProducts_VipUser() {
        // given
        User user = createUser(1L, UserGrade.VIP);
        Cart cart = createCart(1L);

        // 전자제품: 100,000원 → 5% (카테고리 우선) = 5,000 포인트
        addItemToCart(cart, 1L, new BigDecimal("100000"), 1);

        // 가구(일반): 100,000원 → 3% (VIP 적용) = 3,000 포인트
        addItemToCart(cart, 2L, new BigDecimal("100000"), 1);

        // 도서: 50,000원 → 10% (카테고리 우선) = 5,000 포인트
        addItemToCart(cart, 3L, new BigDecimal("50000"), 1);

        Map<Long, Category> categoryMap = Map.of(
            1L, createCategory(1L, "전자제품"),
            2L, createCategory(2L, "가구"),
            3L, createCategory(3L, "도서")
        );

        // when
        BigDecimal actualPoints = calculator.calculateExpectedPoints(
            cart, user, categoryMap, standardPolicy
        );

        // then: 5,000 + 3,000 + 5,000 = 13,000
        assertThat(actualPoints).isEqualByComparingTo(new BigDecimal("13000"));
    }

    @ParameterizedTest
    @MethodSource("provideDecimalTruncationScenarios")
    @DisplayName("소수점 버림 검증")
    void calculateExpectedPoints_DecimalTruncation(
        String scenario,
        String price,
        String categoryName,
        String expectedPoints
    ) {
        // given
        User user = createUser(1L, UserGrade.NORMAL);
        Category category = createCategory(1L, categoryName);
        Cart cart = createCart(1L);
        addItemToCart(cart, 1L, new BigDecimal(price), 1);

        Map<Long, Category> categoryMap = Map.of(1L, category);

        // when
        BigDecimal actualPoints = calculator.calculateExpectedPoints(
            cart, user, categoryMap, standardPolicy
        );

        // then
        assertThat(actualPoints).isEqualByComparingTo(new BigDecimal(expectedPoints));
        assertThat(actualPoints.scale()).isEqualTo(0); // 소수점 없음 검증
    }

    static Stream<Arguments> provideDecimalTruncationScenarios() {
        return Stream.of(
            // 가구(일반 1%): 999원 → 9.99 → 9 포인트
            Arguments.of("소수점 버림 - 999원", "999", "가구", "9"),

            // 전자제품(5%): 1999원 → 99.95 → 99 포인트
            Arguments.of("소수점 버림 - 1999원", "1999", "전자제품", "99"),

            // 도서(10%): 12345원 → 1234.5 → 1234 포인트
            Arguments.of("소수점 버림 - 12345원", "12345", "도서", "1234"),

            // 의류(3%): 33333원 → 999.99 → 999 포인트
            Arguments.of("소수점 버림 - 33333원", "33333", "의류", "999")
        );
    }

    @Test
    @DisplayName("빈 장바구니는 0 포인트")
    void calculateExpectedPoints_EmptyCart_ReturnsZero() {
        // given
        User user = createUser(1L, UserGrade.NORMAL);
        Cart cart = createCart(1L);
        Map<Long, Category> categoryMap = Map.of();

        // when
        BigDecimal actualPoints = calculator.calculateExpectedPoints(
            cart, user, categoryMap, standardPolicy
        );

        // then
        assertThat(actualPoints).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("카테고리가 없는 상품은 기본 적립률 적용")
    void calculateExpectedPoints_UnknownCategory_UsesDefaultRate() {
        // given
        User normalUser = createUser(1L, UserGrade.NORMAL);
        Cart cart = createCart(1L);
        addItemToCart(cart, 1L, new BigDecimal("100000"), 1);

        // 카테고리 맵에 없는 카테고리
        Category unknownCategory = createCategory(1L, "알수없음");
        Map<Long, Category> categoryMap = Map.of(1L, unknownCategory);

        // when
        BigDecimal actualPoints = calculator.calculateExpectedPoints(
            cart, normalUser, categoryMap, standardPolicy
        );

        // then: 기본 1% 적용 → 1,000 포인트
        assertThat(actualPoints).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("VIP가 카테고리 없는 상품 구매 시 VIP 적립률 적용")
    void calculateExpectedPoints_VipWithUnknownCategory_UsesVipRate() {
        // given
        User vipUser = createUser(1L, UserGrade.VIP);
        Cart cart = createCart(1L);
        addItemToCart(cart, 1L, new BigDecimal("100000"), 1);

        Category unknownCategory = createCategory(1L, "알수없음");
        Map<Long, Category> categoryMap = Map.of(1L, unknownCategory);

        // when
        BigDecimal actualPoints = calculator.calculateExpectedPoints(
            cart, vipUser, categoryMap, standardPolicy
        );

        // then: VIP 3% 적용 → 3,000 포인트
        assertThat(actualPoints).isEqualByComparingTo(new BigDecimal("3000"));
    }

    @Test
    @DisplayName("VIP 사용자는 카테고리 적립률과 VIP 적립률 중 큰 값이 적용된다")
    void calculateExpectedPoints_VipMinimumGuarantee() {
        // given
        User vipUser = createUser(1L, UserGrade.VIP);
        Cart cart = createCart(1L);

        // 식품(2%) vs VIP(3%) → VIP 우선
        addItemToCart(cart, 1L, new BigDecimal("100000"), 1);

        Category foodCategory = createCategory(1L, "식품");
        Map<Long, Category> categoryMap = Map.of(1L, foodCategory);

        // when
        BigDecimal actualPoints = calculator.calculateExpectedPoints(
            cart, vipUser, categoryMap, standardPolicy
        );

        // then: VIP 3% 적용 → 3,000 포인트 (식품 2%보다 큼)
        assertThat(actualPoints).isEqualByComparingTo(new BigDecimal("3000"));
    }

    @Test
    @DisplayName("커스텀 정책으로 포인트 계산")
    void calculateExpectedPoints_CustomPolicy() {
        // given
        Map<String, BigDecimal> customCategoryRateMap = new HashMap<>();
        customCategoryRateMap.put("명품", new BigDecimal("0.15"));  // 15%

        PointPolicy customPolicy = new PointPolicy(
            new BigDecimal("0.02"),  // 일반 2%
            new BigDecimal("0.05"),  // VIP 5%
            customCategoryRateMap,
            BigDecimal.ZERO
        );

        User user = createUser(1L, UserGrade.NORMAL);
        Cart cart = createCart(1L);
        addItemToCart(cart, 1L, new BigDecimal("1000000"), 1);  // 100만원

        Category luxuryCategory = createCategory(1L, "명품");
        Map<Long, Category> categoryMap = Map.of(1L, luxuryCategory);

        // when
        BigDecimal actualPoints = calculator.calculateExpectedPoints(
            cart, user, categoryMap, customPolicy
        );

        // then: 명품 15% 적용 → 150,000 포인트
        assertThat(actualPoints).isEqualByComparingTo(new BigDecimal("150000"));
    }

    private User createUser(Long userId, UserGrade grade) {
        return User.builder()
            .userId(userId)
            .email("test@test.com")
            .name("테스트")
            .phoneNumber("010-1234-5678")
            .grade(grade)
            .deleted(false)
            .build();
    }

    private Category createCategory(Long categoryId, String name) {
        return Category.builder()
            .categoryId(categoryId)
            .name(name)
            .description(name + " 카테고리")
            .deleted(false)
            .build();
    }

    private Cart createCart(Long userId) {
        return Cart.builder()
            .cartId(1L)
            .userId(userId)
            .build();
    }

    private void addItemToCart(Cart cart, Long productId, BigDecimal price, int quantity) {
        cart.addItem(productId, quantity, Money.from(price), 100);
    }
}