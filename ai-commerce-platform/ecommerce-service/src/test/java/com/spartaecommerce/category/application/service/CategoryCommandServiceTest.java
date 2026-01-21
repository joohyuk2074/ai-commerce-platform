package com.spartaecommerce.category.application.service;

import com.spartaecommerce.category.application.dto.commnad.CategoryRegisterCommand;
import com.spartaecommerce.common.domain.category.Category;
import com.spartaecommerce.category.fake.FakeCategoryClosurePort;
import com.spartaecommerce.category.fake.FakeCategoryRepository;
import com.spartaecommerce.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("카테고리 명령 서비스 테스트 - 고전파 방식")
class CategoryCommandServiceTest {

    private FakeCategoryRepository categoryRepository;
    private FakeCategoryClosurePort categoryClosurePort;
    private CategoryCommandService categoryCommandService;

    @BeforeEach
    void setUp() {
        categoryRepository = new FakeCategoryRepository();
        categoryClosurePort = new FakeCategoryClosurePort();
        categoryCommandService = new CategoryCommandService(
            categoryRepository,
            categoryRepository,
            categoryClosurePort
        );
    }

    @Test
    @DisplayName("카테고리 등록 성공 - 부모가 있는 하위 카테고리")
    void register_Success_WithParent() {
        // given - 부모 카테고리를 먼저 생성
        Long parentId = categoryRepository.save(
            Category.builder()
                .name("전자제품")
                .description("전자제품 카테고리")
                .parentCategoryId(null)
                .deleted(false)
                .build()
        );
        categoryClosurePort.insertClosureForNewCategory(parentId, null);

        CategoryRegisterCommand command = new CategoryRegisterCommand(
            "노트북",
            "노트북 카테고리",
            parentId
        );

        // when
        Long categoryId = categoryCommandService.register(command);

        // then - 상태 기반 검증
        assertThat(categoryId).isNotNull();

        // 카테고리가 실제로 저장되었는지 확인
        Category savedCategory = categoryRepository.getById(categoryId);
        assertThat(savedCategory.getName()).isEqualTo("노트북");
        assertThat(savedCategory.getDescription()).isEqualTo("노트북 카테고리");
        assertThat(savedCategory.getParentCategoryId()).isEqualTo(parentId);
        assertThat(savedCategory.isDeleted()).isFalse();

        // Closure Table에 관계가 올바르게 생성되었는지 확인
        // 1. 자기 자신과의 관계 (depth=0)
        assertThat(categoryClosurePort.hasRelation(categoryId, categoryId)).isTrue();
        assertThat(categoryClosurePort.getDepth(categoryId, categoryId)).isEqualTo(0);

        // 2. 부모와의 관계 (depth=1)
        assertThat(categoryClosurePort.hasRelation(parentId, categoryId)).isTrue();
        assertThat(categoryClosurePort.getDepth(parentId, categoryId)).isEqualTo(1);
    }

    @Test
    @DisplayName("카테고리 등록 성공 - 부모 없는 최상위 카테고리")
    void register_Success_RootCategory() {
        // given
        CategoryRegisterCommand command = new CategoryRegisterCommand(
            "의류",
            "의류 카테고리",
            null
        );

        // when
        Long categoryId = categoryCommandService.register(command);

        // then
        assertThat(categoryId).isNotNull();

        Category savedCategory = categoryRepository.getById(categoryId);
        assertThat(savedCategory.getName()).isEqualTo("의류");
        assertThat(savedCategory.getParentCategoryId()).isNull();
        assertThat(savedCategory.isDeleted()).isFalse();

        // Closure Table에 자기 자신과의 관계만 존재
        assertThat(categoryClosurePort.hasRelation(categoryId, categoryId)).isTrue();
        assertThat(categoryClosurePort.getDepth(categoryId, categoryId)).isEqualTo(0);
    }

    @Test
    @DisplayName("카테고리 등록 성공 - 3단계 계층 구조")
    void register_Success_ThreeLevelHierarchy() {
        // given - 전자제품 > 컴퓨터 > 노트북 계층 구조 생성
        Long electronicsId = categoryRepository.save(
            Category.builder()
                .name("전자제품")
                .description("전자제품 카테고리")
                .parentCategoryId(null)
                .deleted(false)
                .build()
        );
        categoryClosurePort.insertClosureForNewCategory(electronicsId, null);

        Long computerId = categoryRepository.save(
            Category.builder()
                .name("컴퓨터")
                .description("컴퓨터 카테고리")
                .parentCategoryId(electronicsId)
                .deleted(false)
                .build()
        );
        categoryClosurePort.insertClosureForNewCategory(computerId, electronicsId);

        CategoryRegisterCommand command = new CategoryRegisterCommand(
            "노트북",
            "노트북 카테고리",
            computerId
        );

        // when
        Long laptopId = categoryCommandService.register(command);

        // then
        Category laptop = categoryRepository.getById(laptopId);
        assertThat(laptop.getName()).isEqualTo("노트북");
        assertThat(laptop.getParentCategoryId()).isEqualTo(computerId);

        // Closure Table 검증 - 모든 조상과의 관계가 생성되어야 함
        // 자기 자신 (depth=0)
        assertThat(categoryClosurePort.getDepth(laptopId, laptopId)).isEqualTo(0);
        // 직계 부모 (depth=1)
        assertThat(categoryClosurePort.getDepth(computerId, laptopId)).isEqualTo(1);
        // 조부모 (depth=2)
        assertThat(categoryClosurePort.getDepth(electronicsId, laptopId)).isEqualTo(2);
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 중복된 이름")
    void register_Failure_DuplicateName() {
        // given
        categoryRepository.save(
            Category.builder()
                .name("전자제품")
                .description("기존 카테고리")
                .deleted(false)
                .build()
        );

        CategoryRegisterCommand command = new CategoryRegisterCommand(
            "전자제품",
            "중복된 이름의 카테고리",
            null
        );

        int initialSize = categoryRepository.size();

        // when & then
        assertThatThrownBy(() -> categoryCommandService.register(command))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("전자제품");

        // 카테고리가 추가로 저장되지 않았는지 확인
        assertThat(categoryRepository.size()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 존재하지 않는 부모 카테고리")
    void register_Failure_ParentNotFound() {
        // given
        Long nonExistentParentId = 999L;
        CategoryRegisterCommand command = new CategoryRegisterCommand(
            "노트북",
            "노트북 카테고리",
            nonExistentParentId
        );

        int initialSize = categoryRepository.size();

        // when & then
        assertThatThrownBy(() -> categoryCommandService.register(command))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("999");

        // 카테고리가 저장되지 않았는지 확인
        assertThat(categoryRepository.size()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("카테고리 삭제 성공 - 자식이 없는 카테고리")
    void delete_Success_NoChildren() {
        // given
        Long categoryId = categoryRepository.save(
            Category.builder()
                .name("전자제품")
                .description("전자제품 카테고리")
                .deleted(false)
                .build()
        );
        categoryClosurePort.insertClosureForNewCategory(categoryId, null);

        // when
        categoryCommandService.delete(categoryId);

        // then
        Category deletedCategory = categoryRepository.getById(categoryId);
        assertThat(deletedCategory.isDeleted()).isTrue();

        // Closure Table 엔트리가 삭제되었는지 확인
        assertThat(categoryClosurePort.hasRelation(categoryId, categoryId)).isFalse();
    }

    @Test
    @DisplayName("카테고리 삭제 성공 - 삭제된 자식만 있는 경우")
    void delete_Success_WithDeletedChildren() {
        // given
        Long parentId = categoryRepository.save(
            Category.builder()
                .name("전자제품")
                .description("전자제품 카테고리")
                .deleted(false)
                .build()
        );

        Long childId = categoryRepository.save(
            Category.builder()
                .name("노트북")
                .description("노트북 카테고리")
                .parentCategoryId(parentId)
                .deleted(true)  // 이미 삭제된 자식
                .build()
        );

        categoryClosurePort.insertClosureForNewCategory(parentId, null);
        categoryClosurePort.insertClosureForNewCategory(childId, parentId);

        // when
        categoryCommandService.delete(parentId);

        // then
        Category deletedCategory = categoryRepository.getById(parentId);
        assertThat(deletedCategory.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 활성화된 자식 카테고리가 있는 경우")
    void delete_Failure_HasActiveChildren() {
        // given
        Long parentId = categoryRepository.save(
            Category.builder()
                .name("전자제품")
                .description("전자제품 카테고리")
                .deleted(false)
                .build()
        );

        Long childId = categoryRepository.save(
            Category.builder()
                .name("노트북")
                .description("노트북 카테고리")
                .parentCategoryId(parentId)
                .deleted(false)  // 활성화된 자식
                .build()
        );

        categoryClosurePort.insertClosureForNewCategory(parentId, null);
        categoryClosurePort.insertClosureForNewCategory(childId, parentId);

        // when & then
        assertThatThrownBy(() -> categoryCommandService.delete(parentId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("active children");

        // 카테고리가 삭제되지 않았는지 확인
        Category category = categoryRepository.getById(parentId);
        assertThat(category.isDeleted()).isFalse();

        // Closure Table 엔트리가 유지되는지 확인
        assertThat(categoryClosurePort.hasRelation(parentId, parentId)).isTrue();
        assertThat(categoryClosurePort.hasRelation(parentId, childId)).isTrue();
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 이미 삭제된 카테고리")
    void delete_Failure_AlreadyDeleted() {
        // given
        Long categoryId = categoryRepository.save(
            Category.builder()
                .name("전자제품")
                .description("전자제품 카테고리")
                .deleted(true)  // 이미 삭제됨
                .build()
        );

        // when & then
        assertThatThrownBy(() -> categoryCommandService.delete(categoryId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Already deleted");
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 존재하지 않는 카테고리")
    void delete_Failure_CategoryNotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> categoryCommandService.delete(nonExistentId))
            .isInstanceOf(BusinessException.class);
    }
}
