package com.spartaecommerce.cart.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.cart.presentation.controller.dto.request.CartAddItemRequest;
import com.spartaecommerce.category.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import com.spartaecommerce.category.infrastructure.persistence.jpa.repository.CategoryJpaRepository;
import com.spartaecommerce.product.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import com.spartaecommerce.product.infrastructure.persistence.jpa.repository.ProductJpaRepository;
import com.spartaecommerce.user.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.spartaecommerce.user.infrastructure.persistence.jpa.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("CartController")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    @DisplayName("최대 품목 수(20개) 초과 시 400 응답 반환")
    void addItem_ExceedMaxItems_Returns400() throws Exception {
        // given: 사용자 생성
        com.spartaecommerce.user.domain.entity.User user = com.spartaecommerce.user.domain.entity.User.createNew(
            "test@example.com",
            "Test User",
            "010-1234-5678"
        );
        UserJpaEntity userEntity = UserJpaEntity.from(user);
        userEntity = userJpaRepository.save(userEntity);
        Long userId = userEntity.getUserId();

        // given: 카테고리 생성
        com.spartaecommerce.category.domain.entity.Category category = com.spartaecommerce.category.domain.entity.Category.createNew(
            "전자제품",
            "전자제품 카테고리",
            null
        );
        CategoryJpaEntity categoryEntity = CategoryJpaEntity.from(category);
        categoryEntity = categoryJpaRepository.save(categoryEntity);
        Long categoryId = categoryEntity.getCategoryId();

        // given: 20개의 상품을 장바구니에 추가
        for (int i = 1; i <= 20; i++) {
            com.spartaecommerce.product.domain.entity.Product product = com.spartaecommerce.product.domain.entity.Product.createNew(
                "상품" + i,
                "설명" + i,
                com.spartaecommerce.common.domain.Money.from(new BigDecimal("1000")),
                100,
                categoryId
            );
            ProductJpaEntity productEntity = ProductJpaEntity.from(product);
            productEntity = productJpaRepository.save(productEntity);

            CartAddItemRequest request = new CartAddItemRequest(productEntity.getProductId(), 1);

            mockMvc.perform(post("/api/v1/carts/" + userId + "/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        }

        // given: 21번째 상품 생성
        com.spartaecommerce.product.domain.entity.Product extraProduct = com.spartaecommerce.product.domain.entity.Product.createNew(
            "상품21",
            "설명21",
            com.spartaecommerce.common.domain.Money.from(new BigDecimal("1000")),
            100,
            categoryId
        );
        ProductJpaEntity extraProductEntity = ProductJpaEntity.from(extraProduct);
        extraProductEntity = productJpaRepository.save(extraProductEntity);

        CartAddItemRequest request = new CartAddItemRequest(extraProductEntity.getProductId(), 1);

        // when & then: 21번째 상품 추가 시도 시 400 응답
        mockMvc.perform(post("/api/v1/carts/" + userId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("카테고리별 가중치가 반영된 예상 포인트 반환 - 전자제품 5%")
    void getCart_WithElectronicsCategoryProduct_ReturnsExpectedPoints() throws Exception {
        // given: 사용자 생성
        com.spartaecommerce.user.domain.entity.User user = com.spartaecommerce.user.domain.entity.User.createNew(
            "test2@example.com",
            "Test User 2",
            "010-2222-3333"
        );
        UserJpaEntity userEntity = UserJpaEntity.from(user);
        userEntity = userJpaRepository.save(userEntity);
        Long userId = userEntity.getUserId();

        // given: 전자제품 카테고리 생성 (5% 포인트 적립)
        com.spartaecommerce.category.domain.entity.Category category = com.spartaecommerce.category.domain.entity.Category.createNew(
            "전자제품",
            "전자제품 카테고리",
            null
        );
        CategoryJpaEntity categoryEntity = CategoryJpaEntity.from(category);
        categoryEntity = categoryJpaRepository.save(categoryEntity);
        Long categoryId = categoryEntity.getCategoryId();

        // given: 상품 생성 (가격: 10,000원)
        com.spartaecommerce.product.domain.entity.Product product = com.spartaecommerce.product.domain.entity.Product.createNew(
            "노트북",
            "고성능 노트북",
            com.spartaecommerce.common.domain.Money.from(new BigDecimal("10000")),
            100,
            categoryId
        );
        ProductJpaEntity productEntity = ProductJpaEntity.from(product);
        productEntity = productJpaRepository.save(productEntity);

        // given: 장바구니에 상품 추가 (수량: 2)
        CartAddItemRequest addRequest = new CartAddItemRequest(productEntity.getProductId(), 2);
        mockMvc.perform(post("/api/v1/carts/" + userId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)))
            .andExpect(status().isOk());

        // when & then: 장바구니 조회 시 예상 포인트 확인
        // 총 금액: 10,000 * 2 = 20,000
        // 예상 포인트: 20,000 * 0.05 = 1,000
        mockMvc.perform(get("/api/v1/carts/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalAmount").value(20000.00))
            .andExpect(jsonPath("$.data.expectedPoints").value(1000));
    }

    @Test
    @DisplayName("재고 부족 시 400 응답 반환")
    void addItem_InsufficientStock_Returns400() throws Exception {
        // given: 사용자 생성
        com.spartaecommerce.user.domain.entity.User user = com.spartaecommerce.user.domain.entity.User.createNew(
            "test3@example.com",
            "Test User 3",
            "010-3333-4444"
        );
        UserJpaEntity userEntity = UserJpaEntity.from(user);
        userEntity = userJpaRepository.save(userEntity);
        Long userId = userEntity.getUserId();

        // given: 카테고리 생성
        com.spartaecommerce.category.domain.entity.Category category = com.spartaecommerce.category.domain.entity.Category.createNew(
            "전자제품",
            "전자제품 카테고리",
            null
        );
        CategoryJpaEntity categoryEntity = CategoryJpaEntity.from(category);
        categoryEntity = categoryJpaRepository.save(categoryEntity);

        // given: 재고가 5개인 상품 생성
        com.spartaecommerce.product.domain.entity.Product product = com.spartaecommerce.product.domain.entity.Product.createNew(
            "제한된 재고 상품",
            "재고가 부족한 상품",
            com.spartaecommerce.common.domain.Money.from(new BigDecimal("10000")),
            5,  // 재고: 5개
            categoryEntity.getCategoryId()
        );
        ProductJpaEntity productEntity = ProductJpaEntity.from(product);
        productEntity = productJpaRepository.save(productEntity);

        // when & then: 6개 주문 시도 시 400 응답
        CartAddItemRequest request = new CartAddItemRequest(productEntity.getProductId(), 6);

        mockMvc.perform(post("/api/v1/carts/" + userId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}