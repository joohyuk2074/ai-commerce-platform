package com.spartaecommerce.product.adapter.out.persistence;

import com.spartaecommerce.common.JpaTestSupport;
import com.spartaecommerce.common.domain.CustomPageable;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.product.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import com.spartaecommerce.product.application.dto.query.ProductSearchQuery;
import com.spartaecommerce.product.domain.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductPersistenceQueryAdapter 테스트")
class ProductPersistenceQueryAdapterTest extends JpaTestSupport {

    private ProductPersistenceQueryAdapter sut;

    @BeforeEach
    void setUp() {
        sut = new ProductPersistenceQueryAdapter(queryFactory);
    }

    @Nested
    @DisplayName("상품 검색 시")
    class Search {

        @Test
        @DisplayName("조건 없이 전체 상품을 조회한다")
        void search_WithNoConditions_ReturnsAllProducts() {
            // given
            ProductJpaEntity product1 = createProductEntity("클린코드", new BigDecimal("30000"), 1L, true);
            ProductJpaEntity product2 = createProductEntity("리팩터링", new BigDecimal("35000"), 1L, true);
            ProductJpaEntity product3 = createProductEntity("DDD", new BigDecimal("40000"), 2L, true);
            persistAndFlush(product1);
            persistAndFlush(product2);
            persistAndFlush(product3);
            flushAndClear();

            ProductSearchQuery query = new ProductSearchQuery(
                null,
                null,
                null,
                null,
                null,
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("카테고리 ID로 필터링하여 조회한다")
        void search_WithCategoryId_ReturnsFilteredProducts() {
            // given
            Long targetCategoryId = 1L;
            ProductJpaEntity product1 = createProductEntity("클린코드", new BigDecimal("30000"), targetCategoryId, true);
            ProductJpaEntity product2 = createProductEntity("리팩터링", new BigDecimal("35000"), targetCategoryId, true);
            ProductJpaEntity product3 = createProductEntity("DDD", new BigDecimal("40000"), 2L, true);
            persistAndFlush(product1);
            persistAndFlush(product2);
            persistAndFlush(product3);
            flushAndClear();

            ProductSearchQuery query = new ProductSearchQuery(
                targetCategoryId,
                null,
                null,
                null,
                null,
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(Product::getCategoryId)
                .containsOnly(targetCategoryId);
        }

        @Test
        @DisplayName("주문 가능 여부로 필터링하여 조회한다")
        void search_WithIsOrderable_ReturnsFilteredProducts() {
            // given
            ProductJpaEntity orderableProduct1 = createProductEntity("클린코드", new BigDecimal("30000"), 1L, true);
            ProductJpaEntity orderableProduct2 = createProductEntity("리팩터링", new BigDecimal("35000"), 1L, true);
            ProductJpaEntity notOrderableProduct = createProductEntity("절판도서", new BigDecimal("40000"), 1L, false);
            persistAndFlush(orderableProduct1);
            persistAndFlush(orderableProduct2);
            persistAndFlush(notOrderableProduct);
            flushAndClear();

            ProductSearchQuery query = new ProductSearchQuery(
                null,
                true,
                null,
                null,
                null,
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(Product::isOrderable)
                .containsOnly(true);
        }

        @Test
        @DisplayName("최소 가격으로 필터링하여 조회한다")
        void search_WithMinPrice_ReturnsFilteredProducts() {
            // given
            ProductJpaEntity product1 = createProductEntity("클린코드", new BigDecimal("20000"), 1L, true);
            ProductJpaEntity product2 = createProductEntity("리팩터링", new BigDecimal("35000"), 1L, true);
            ProductJpaEntity product3 = createProductEntity("DDD", new BigDecimal("40000"), 1L, true);
            persistAndFlush(product1);
            persistAndFlush(product2);
            persistAndFlush(product3);
            flushAndClear();

            Money minPrice = Money.from(new BigDecimal("30000"));
            ProductSearchQuery query = new ProductSearchQuery(
                null,
                null,
                minPrice,
                null,
                null,
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(product -> product.getPrice().amount())
                .allMatch(price -> price.compareTo(minPrice.amount()) >= 0);
        }

        @Test
        @DisplayName("최대 가격으로 필터링하여 조회한다")
        void search_WithMaxPrice_ReturnsFilteredProducts() {
            // given
            ProductJpaEntity product1 = createProductEntity("클린코드", new BigDecimal("20000"), 1L, true);
            ProductJpaEntity product2 = createProductEntity("리팩터링", new BigDecimal("35000"), 1L, true);
            ProductJpaEntity product3 = createProductEntity("DDD", new BigDecimal("40000"), 1L, true);
            persistAndFlush(product1);
            persistAndFlush(product2);
            persistAndFlush(product3);
            flushAndClear();

            Money maxPrice = Money.from(new BigDecimal("35000"));
            ProductSearchQuery query = new ProductSearchQuery(
                null,
                null,
                null,
                maxPrice,
                null,
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(product -> product.getPrice().amount())
                .allMatch(price -> price.compareTo(maxPrice.amount()) <= 0);
        }

        @Test
        @DisplayName("가격 범위로 필터링하여 조회한다")
        void search_WithPriceRange_ReturnsFilteredProducts() {
            // given
            ProductJpaEntity product1 = createProductEntity("저렴한 책", new BigDecimal("10000"), 1L, true);
            ProductJpaEntity product2 = createProductEntity("클린코드", new BigDecimal("30000"), 1L, true);
            ProductJpaEntity product3 = createProductEntity("리팩터링", new BigDecimal("35000"), 1L, true);
            ProductJpaEntity product4 = createProductEntity("비싼 책", new BigDecimal("50000"), 1L, true);
            persistAndFlush(product1);
            persistAndFlush(product2);
            persistAndFlush(product3);
            persistAndFlush(product4);
            flushAndClear();

            Money minPrice = Money.from(new BigDecimal("25000"));
            Money maxPrice = Money.from(new BigDecimal("40000"));
            ProductSearchQuery query = new ProductSearchQuery(
                null,
                null,
                minPrice,
                maxPrice,
                null,
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("클린코드", "리팩터링");
        }

        @Test
        @DisplayName("키워드로 상품명을 검색한다")
        void search_WithKeyword_ReturnsMatchingProducts() {
            // given
            ProductJpaEntity product1 = createProductEntity("클린 코드", new BigDecimal("30000"), 1L, true);
            ProductJpaEntity product2 = createProductEntity("리팩터링", new BigDecimal("35000"), 1L, true);
            ProductJpaEntity product3 = createProductEntity("클린 아키텍처", new BigDecimal("40000"), 1L, true);
            persistAndFlush(product1);
            persistAndFlush(product2);
            persistAndFlush(product3);
            flushAndClear();

            ProductSearchQuery query = new ProductSearchQuery(
                null,
                null,
                null,
                null,
                "클린",
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("클린 코드", "클린 아키텍처");
        }

        @Test
        @DisplayName("키워드 검색은 대소문자를 구분하지 않는다")
        void search_WithKeyword_IsCaseInsensitive() {
            // given
            ProductJpaEntity product1 = createProductEntity("Clean Code", new BigDecimal("30000"), 1L, true);
            ProductJpaEntity product2 = createProductEntity("Refactoring", new BigDecimal("35000"), 1L, true);
            persistAndFlush(product1);
            persistAndFlush(product2);
            flushAndClear();

            ProductSearchQuery query = new ProductSearchQuery(
                null,
                null,
                null,
                null,
                "clean",
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("여러 조건을 조합하여 검색한다")
        void search_WithMultipleConditions_ReturnsMatchingProducts() {
            // given
            ProductJpaEntity product1 = createProductEntity("클린 코드", new BigDecimal("30000"), 1L, true);
            ProductJpaEntity product2 = createProductEntity("리팩터링", new BigDecimal("35000"), 1L, true);
            ProductJpaEntity product3 = createProductEntity("클린 아키텍처", new BigDecimal("40000"), 2L, true);
            ProductJpaEntity product4 = createProductEntity("클린 소프트웨어", new BigDecimal("25000"), 1L, false);
            persistAndFlush(product1);
            persistAndFlush(product2);
            persistAndFlush(product3);
            persistAndFlush(product4);
            flushAndClear();

            ProductSearchQuery query = new ProductSearchQuery(
                1L,                                          // categoryId
                true,                                         // isOrderable
                Money.from(new BigDecimal("28000")),         // minPrice
                Money.from(new BigDecimal("35000")),         // maxPrice
                "클린",                                       // keyword
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("클린 코드");
        }

        @Test
        @DisplayName("페이징을 적용하여 조회한다")
        void search_WithPaging_ReturnsPaginatedResults() {
            // given
            for (int i = 1; i <= 15; i++) {
                ProductJpaEntity product = createProductEntity(
                    "상품" + i,
                    new BigDecimal("10000").add(new BigDecimal(i * 1000)),
                    1L,
                    true
                );
                persistAndFlush(product);
            }
            flushAndClear();

            // 첫 번째 페이지 (0-based index)
            ProductSearchQuery firstPageQuery = new ProductSearchQuery(
                null,
                null,
                null,
                null,
                null,
                new CustomPageable(0, 5, "productId", "DESC", null)
            );

            // when
            Page<Product> firstPage = sut.search(firstPageQuery);

            // then
            assertThat(firstPage.getTotalElements()).isEqualTo(15);
            assertThat(firstPage.getTotalPages()).isEqualTo(3);
            assertThat(firstPage.getContent()).hasSize(5);
            assertThat(firstPage.getNumber()).isEqualTo(0);
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("두 번째 페이지를 조회한다")
        void search_WithPaging_ReturnsSecondPage() {
            // given
            for (int i = 1; i <= 15; i++) {
                ProductJpaEntity product = createProductEntity(
                    "상품" + i,
                    new BigDecimal("10000").add(new BigDecimal(i * 1000)),
                    1L,
                    true
                );
                persistAndFlush(product);
            }
            flushAndClear();

            // 두 번째 페이지
            ProductSearchQuery secondPageQuery = new ProductSearchQuery(
                null,
                null,
                null,
                null,
                null,
                new CustomPageable(1, 5, "productId", "DESC", null)
            );

            // when
            Page<Product> secondPage = sut.search(secondPageQuery);

            // then
            assertThat(secondPage.getTotalElements()).isEqualTo(15);
            assertThat(secondPage.getContent()).hasSize(5);
            assertThat(secondPage.getNumber()).isEqualTo(1);
            assertThat(secondPage.hasNext()).isTrue();
            assertThat(secondPage.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("삭제된 상품은 검색 결과에 포함되지 않는다")
        void search_ExcludesDeletedProducts() {
            // given
            ProductJpaEntity activeProduct = createProductEntity("클린코드", new BigDecimal("30000"), 1L, true);
            ProductJpaEntity deletedProduct = createDeletedProductEntity("리팩터링", new BigDecimal("35000"), 1L);
            persistAndFlush(activeProduct);
            persistAndFlush(deletedProduct);
            flushAndClear();

            ProductSearchQuery query = new ProductSearchQuery(
                null,
                null,
                null,
                null,
                null,
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("클린코드");
        }

        @Test
        @DisplayName("조건에 맞는 상품이 없으면 빈 결과를 반환한다")
        void search_WithNoMatches_ReturnsEmptyResult() {
            // given
            ProductJpaEntity product = createProductEntity("클린코드", new BigDecimal("30000"), 1L, true);
            persistAndFlush(product);
            flushAndClear();

            ProductSearchQuery query = new ProductSearchQuery(
                999L,  // 존재하지 않는 카테고리
                null,
                null,
                null,
                null,
                CustomPageable.ofDefaults()
            );

            // when
            Page<Product> result = sut.search(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }
    }

    private ProductJpaEntity createProductEntity(
        String name,
        BigDecimal price,
        Long categoryId,
        boolean isOrderable
    ) {
        Product product = Product.builder()
            .name(name)
            .description("설명: " + name)
            .price(Money.from(price))
            .stock(100)
            .categoryId(categoryId)
            .isOrderable(isOrderable)
            .deleted(false)
            .build();

        return ProductJpaEntity.from(product);
    }

    private ProductJpaEntity createDeletedProductEntity(
        String name,
        BigDecimal price,
        Long categoryId
    ) {
        Product product = Product.builder()
            .name(name)
            .description("설명: " + name)
            .price(Money.from(price))
            .stock(100)
            .categoryId(categoryId)
            .isOrderable(true)
            .deleted(true)
            .build();

        return ProductJpaEntity.from(product);
    }
}
