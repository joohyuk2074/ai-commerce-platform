package com.spartaecommerce.order.application;

import com.spartaecommerce.category.domain.port.out.CategoryFakeRepository;
import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.util.FakeDateTimeHolder;
import com.spartaecommerce.order.domain.entity.Order;
import com.spartaecommerce.order.domain.entity.OrderHistory;
import com.spartaecommerce.order.domain.entity.OrderItem;
import com.spartaecommerce.order.domain.entity.OrderStatus;
import com.spartaecommerce.order.domain.port.out.OrderFakeRepository;
import com.spartaecommerce.order.domain.port.out.OrderHistoryFakeRepository;
import com.spartaecommerce.pointwallet.domain.service.PointCalculator;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.ProductFakeRepository;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.entity.UserGrade;
import com.spartaecommerce.user.domain.repository.UserFakeRepository;
import com.spartaecommerce.pointwallet.domain.port.out.PointTransactionFakeRepository;
import com.spartaecommerce.pointwallet.domain.port.out.PointWalletFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@DisplayName("OrderService")
class OrderServiceTest {

    protected OrderCommandService sut;

    protected UserFakeRepository userRepository;
    protected OrderFakeRepository orderRepository;
    protected ProductFakeRepository productRepository;
    protected OrderHistoryFakeRepository orderHistoryRepository;
    protected CategoryFakeRepository categoryRepository;
    protected PointWalletFakeRepository pointWalletRepository;
    protected PointTransactionFakeRepository pointTransactionRepository;
    protected FakeDateTimeHolder dateTimeHolder;
    protected PointsProperties pointsProperties;

    @BeforeEach
    void setUp() {
        userRepository = new UserFakeRepository();
        orderRepository = new OrderFakeRepository();
        productRepository = new ProductFakeRepository();
        orderHistoryRepository = new OrderHistoryFakeRepository();
        categoryRepository = new CategoryFakeRepository();
        pointWalletRepository = new PointWalletFakeRepository();
        pointTransactionRepository = new PointTransactionFakeRepository();
        dateTimeHolder = new FakeDateTimeHolder(LocalDateTime.of(2025, 11, 9, 20, 31));

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

        // OrderItemProcessor 생성
        OrderItemProcessor orderItemProcessor = new OrderItemProcessor(productRepository, categoryRepository);

        // OrderPointProcessor 생성
        PointCalculator pointCalculator = new PointCalculator();
        OrderPointProcessor orderPointProcessor = new OrderPointProcessor(
            pointCalculator,
            pointWalletRepository,
            pointWalletRepository,
            pointTransactionRepository,
            pointsProperties,
            dateTimeHolder
        );

        // OrderHistoryRecorder 생성
        OrderHistoryRecorder orderHistoryRecorder = new OrderHistoryRecorder(orderHistoryRepository, dateTimeHolder);

        sut = new OrderCommandService(
            userRepository,
            orderRepository,
            orderRepository,
            productRepository,
            orderItemProcessor,
            orderPointProcessor,
            orderHistoryRecorder
        );
    }

    @AfterEach
    void tearDown() {
        orderHistoryRepository.clear();
        orderRepository.clear();
        productRepository.clear();
        userRepository.clear();
        categoryRepository.clear();
        pointWalletRepository.clear();
        pointTransactionRepository.clear();
    }

    protected User createUser() {
        return User.builder()
            .grade(UserGrade.NORMAL)
            .build();
    }

    protected Long createUserWithWallet() {
        User user = createUser();
        Long userId = userRepository.save(user);
        pointWalletRepository.save(com.spartaecommerce.pointwallet.domain.entity.PointWallet.createNew(userId));
        return userId;
    }

    protected Product createProduct(String name, int stock) {
        return Product.builder()
            .name(name)
            .description("설명")
            .price(Money.from(BigDecimal.valueOf(30000)))
            .stock(stock)
            .categoryId(1L)
            .deleted(false)
            .build();
    }

    protected Order createOrder(Long userId, Long productId, Integer quantity) {
        OrderItem orderItem = OrderItem.builder()
            .productId(productId)
            .quantity(quantity)
            .build();
        return Order.builder()
            .userId(userId)
            .orderItems(List.of(orderItem))
            .status(OrderStatus.PENDING)
            .usedPointAmount(Money.zero())
            .earnedPointAmount(Money.zero())
            .build();
    }

    protected OrderHistory createOrderHistory(Long orderId, OrderStatus from, OrderStatus to) {
        return OrderHistory.builder()
            .orderId(orderId)
            .fromStatus(from)
            .toStatus(to)
            .build();
    }
}