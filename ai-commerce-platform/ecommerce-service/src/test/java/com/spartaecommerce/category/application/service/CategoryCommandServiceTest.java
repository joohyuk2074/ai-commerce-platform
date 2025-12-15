package com.spartaecommerce.category.application.service;

import com.spartaecommerce.category.application.dto.commnad.CategoryRegisterCommand;
import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.out.CategoryFakeRepository;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.domain.entity.Product;
import com.spartaecommerce.product.domain.port.out.ProductFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CategoryCommandService")
class CategoryCommandServiceTest {

    private CategoryCommandService sut;
    private CategoryFakeRepository categoryRepository;
    private ProductFakeRepository productRepository;

    @BeforeEach
    void setUp() {
        categoryRepository = new CategoryFakeRepository();
        productRepository = new ProductFakeRepository();

        sut = new CategoryCommandService(
            categoryRepository,
            categoryRepository,
            productRepository
        );
    }

    @AfterEach
    void tearDown() {
        categoryRepository.clear();
        productRepository.clear();
    }

    @Nested
    @DisplayName("카테고리 등록 시")
    class Register {

        @Test
        @DisplayName("유효한 정보로 카테고리를 등록한다")
        void register_WithValidData_RegistersCategory() {
            // given
            CategoryRegisterCommand command = new CategoryRegisterCommand(
                "도서",
                "도서 카테고리",
                null
            );

            // when
            Long categoryId = sut.register(command);

            // then
            assertThat(categoryId).isNotNull();

            Category savedCategory = categoryRepository.getById(categoryId);
            assertThat(savedCategory.getName()).isEqualTo("도서");
            assertThat(savedCategory.getDescription()).isEqualTo("도서 카테고리");
            assertThat(savedCategory.getParentCategoryId()).isNull();
            assertThat(savedCategory.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("하위 카테고리를 등록한다")
        void register_WithParentCategory_RegistersSubCategory() {
            // given
            Long parentCategoryId = categoryRepository.save(createCategory("도서"));

            CategoryRegisterCommand command = new CategoryRegisterCommand(
                "프로그래밍",
                "프로그래밍 도서",
                parentCategoryId
            );

            // when
            Long categoryId = sut.register(command);

            // then
            assertThat(categoryId).isNotNull();

            Category savedCategory = categoryRepository.getById(categoryId);
            assertThat(savedCategory.getName()).isEqualTo("프로그래밍");
            assertThat(savedCategory.getDescription()).isEqualTo("프로그래밍 도서");
            assertThat(savedCategory.getParentCategoryId()).isEqualTo(parentCategoryId);
        }

        @Test
        @DisplayName("이미 존재하는 카테고리명으로 등록하면 예외가 발생한다")
        void register_WithDuplicateName_ThrowsException() {
            // given
            String duplicateName = "도서";
            categoryRepository.save(createCategory(duplicateName));

            CategoryRegisterCommand command = new CategoryRegisterCommand(
                duplicateName,
                "다른 설명",
                null
            );

            // when & then
            assertThatThrownBy(() -> sut.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("카테고리 수정 시")
    class Update {

        @Test
        @DisplayName("유효한 정보로 카테고리를 수정한다")
        void update_WithValidData_UpdatesCategory() {
            // given
            Long categoryId = categoryRepository.save(createCategory("도서"));

            CategoryUpdateCommand command = new CategoryUpdateCommand(
                categoryId,
                "책",
                "책 카테고리"
            );

            // when
            sut.update(command);

            // then
            Category updatedCategory = categoryRepository.getById(categoryId);
            assertThat(updatedCategory.getName()).isEqualTo("책");
            assertThat(updatedCategory.getDescription()).isEqualTo("책 카테고리");
        }

        @Test
        @DisplayName("존재하지 않는 카테고리를 수정하면 예외가 발생한다")
        void update_WithNonExistentCategory_ThrowsException() {
            // given
            Long nonExistentCategoryId = 999L;
            CategoryUpdateCommand command = new CategoryUpdateCommand(
                nonExistentCategoryId,
                "새 이름",
                "새 설명"
            );

            // when & then
            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 시")
    class Delete {

        @Test
        @DisplayName("하위 카테고리와 상품이 없는 카테고리는 정상적으로 삭제된다")
        void delete_WithNoChildrenAndProducts_DeletesSuccessfully() {
            // given
            Long categoryId = categoryRepository.save(createCategory("도서"));

            // when
            sut.delete(categoryId);

            // then
            Category deletedCategory = categoryRepository.findById(categoryId).orElse(null);
            assertThat(deletedCategory).isNull();
        }

        @Test
        @DisplayName("하위 카테고리가 있는 카테고리는 삭제할 수 없다")
        void delete_WithActiveChildren_ThrowsException() {
            // given
            Long parentCategoryId = categoryRepository.save(createCategory("도서"));
            Category childCategory = createCategory("프로그래밍");
            Category childCategoryWithParent = Category.builder()
                .name(childCategory.getName())
                .description(childCategory.getDescription())
                .parentCategoryId(parentCategoryId)
                .childrenCategoryIds(childCategory.getChildrenCategoryIds())
                .deleted(childCategory.isDeleted())
                .build();
            categoryRepository.save(childCategoryWithParent);

            // when & then
            assertThatThrownBy(() -> sut.delete(parentCategoryId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }

        @Test
        @DisplayName("상품이 있는 카테고리는 삭제할 수 없다")
        void delete_WithActiveProducts_ThrowsException() {
            // given
            Long categoryId = categoryRepository.save(createCategory("도서"));
            productRepository.save(createProduct("클린코드", categoryId));

            // when & then
            assertThatThrownBy(() -> sut.delete(categoryId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }

        @Test
        @DisplayName("이미 삭제된 카테고리를 삭제하면 예외가 발생한다")
        void delete_AlreadyDeleted_ThrowsException() {
            // given
            Category category = createCategory("도서");
            Long categoryId = categoryRepository.save(category);
            sut.delete(categoryId);

            // when & then
            assertThatThrownBy(() -> sut.delete(categoryId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리를 삭제하면 예외가 발생한다")
        void delete_WithNonExistentCategory_ThrowsException() {
            // given
            Long nonExistentCategoryId = 999L;

            // when & then
            assertThatThrownBy(() -> sut.delete(nonExistentCategoryId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    private Category createCategory(String name) {
        return Category.builder()
            .name(name)
            .description(name + " 카테고리")
            .parentCategoryId(null)
            .childrenCategoryIds(new java.util.ArrayList<>())
            .deleted(false)
            .build();
    }

    private Product createProduct(String name, Long categoryId) {
        return Product.builder()
            .name(name)
            .description("설명")
            .price(Money.from(new BigDecimal("30000")))
            .stock(100)
            .categoryId(categoryId)
            .deleted(false)
            .build();
    }
}
