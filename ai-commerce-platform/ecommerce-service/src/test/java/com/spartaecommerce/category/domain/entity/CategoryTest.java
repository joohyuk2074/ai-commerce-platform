package com.spartaecommerce.category.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Category")
class CategoryTest {

    @Nested
    @DisplayName("카테고리 생성 시")
    class CreateNew {

        @Test
        @DisplayName("유효한 정보로 카테고리를 생성한다")
        void createNew_WithValidData_CreatesCategory() {
            // given
            String name = "도서";
            String description = "도서 카테고리";
            Long parentCategoryId = null;

            // when
            Category category = Category.createNew(name, description, parentCategoryId);

            // then
            assertThat(category.getName()).isEqualTo(name);
            assertThat(category.getDescription()).isEqualTo(description);
            assertThat(category.getParentCategoryId()).isNull();
            assertThat(category.getChildrenCategoryIds()).isEmpty();
            assertThat(category.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("하위 카테고리를 생성한다")
        void createNew_WithParentCategory_CreatesSubCategory() {
            // given
            String name = "프로그래밍";
            String description = "프로그래밍 도서";
            Long parentCategoryId = 1L;

            // when
            Category category = Category.createNew(name, description, parentCategoryId);

            // then
            assertThat(category.getName()).isEqualTo(name);
            assertThat(category.getDescription()).isEqualTo(description);
            assertThat(category.getParentCategoryId()).isEqualTo(parentCategoryId);
            assertThat(category.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("카테고리 수정 시")
    class Update {

        @Test
        @DisplayName("이름과 설명을 수정한다")
        void update_WithValidData_UpdatesCategory() {
            // given
            Category category = Category.createNew("도서", "도서 카테고리", null);
            String newName = "책";
            String newDescription = "책 카테고리";

            // when
            category.update(newName, newDescription);

            // then
            assertThat(category.getName()).isEqualTo(newName);
            assertThat(category.getDescription()).isEqualTo(newDescription);
        }

        @Test
        @DisplayName("null 값으로 수정하면 변경되지 않는다")
        void update_WithNullValues_DoesNotUpdate() {
            // given
            Category category = Category.createNew("도서", "도서 카테고리", null);
            String originalName = category.getName();
            String originalDescription = category.getDescription();

            // when
            category.update(null, null);

            // then
            assertThat(category.getName()).isEqualTo(originalName);
            assertThat(category.getDescription()).isEqualTo(originalDescription);
        }

        @Test
        @DisplayName("빈 문자열로 수정하면 변경되지 않는다")
        void update_WithBlankValues_DoesNotUpdate() {
            // given
            Category category = Category.createNew("도서", "도서 카테고리", null);
            String originalName = category.getName();
            String originalDescription = category.getDescription();

            // when
            category.update("", "");

            // then
            assertThat(category.getName()).isEqualTo(originalName);
            assertThat(category.getDescription()).isEqualTo(originalDescription);
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 시")
    class Delete {

        @Test
        @DisplayName("카테고리를 삭제한다")
        void delete_MarksAsDeleted() {
            // given
            Category category = Category.createNew("도서", "도서 카테고리", null);

            // when
            category.delete();

            // then
            assertThat(category.isDeleted()).isTrue();
        }
    }
}
