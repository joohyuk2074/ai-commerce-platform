package com.spartaecommerce.category.application.service;

import com.spartaecommerce.category.application.dto.result.CategoryTreeNodeResult;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("카테고리 조회 서비스 테스트")
class CategoryQueryServiceTest {

    @Mock
    private LoadCategoryPort loadCategoryPort;

    @InjectMocks
    private CategoryQueryService categoryQueryService;

    private List<Category> testCategories;

    @BeforeEach
    void setUp() {
        // 테스트용 카테고리 트리 구성
        // 전자제품 (1)
        //   ㄴ 컴퓨터 (2)
        //       ㄴ 노트북 (3)
        //       ㄴ 데스크톱 (4)
        //   ㄴ 가전 (5)
        // 의류 (6)
        //   ㄴ 남성의류 (7)

        Category electronics = createCategory(1L, "전자제품", null);
        Category computers = createCategory(2L, "컴퓨터", 1L);
        Category laptops = createCategory(3L, "노트북", 2L);
        Category desktops = createCategory(4L, "데스크톱", 2L);
        Category appliances = createCategory(5L, "가전", 1L);
        Category clothing = createCategory(6L, "의류", null);
        Category mensClothing = createCategory(7L, "남성의류", 6L);

        testCategories = Arrays.asList(
            electronics, computers, laptops, desktops,
            appliances, clothing, mensClothing
        );
    }

    @Test
    @DisplayName("카테고리 트리 조회 - 전체 구조 확인")
    void getCategoryTree_Success_FullHierarchy() {
        // given
        given(loadCategoryPort.findAll()).willReturn(testCategories);

        // when
        List<CategoryTreeNodeResult> result = categoryQueryService.getCategoryTree();

        // then
        assertThat(result).hasSize(2);  // 최상위 카테고리 2개 (전자제품, 의류)

        // 전자제품 트리 검증
        CategoryTreeNodeResult electronics = findNodeById(result, 1L);
        assertThat(electronics).isNotNull();
        assertThat(electronics.getName()).isEqualTo("전자제품");
        assertThat(electronics.getChildren()).hasSize(2);  // 컴퓨터, 가전

        // 컴퓨터 하위 카테고리 검증
        CategoryTreeNodeResult computers = findNodeById(electronics.getChildren(), 2L);
        assertThat(computers).isNotNull();
        assertThat(computers.getName()).isEqualTo("컴퓨터");
        assertThat(computers.getChildren()).hasSize(2);  // 노트북, 데스크톱

        // 노트북 검증 (리프 노드)
        CategoryTreeNodeResult laptops = findNodeById(computers.getChildren(), 3L);
        assertThat(laptops).isNotNull();
        assertThat(laptops.getName()).isEqualTo("노트북");
        assertThat(laptops.getChildren()).isEmpty();

        // 의류 트리 검증
        CategoryTreeNodeResult clothing = findNodeById(result, 6L);
        assertThat(clothing).isNotNull();
        assertThat(clothing.getName()).isEqualTo("의류");
        assertThat(clothing.getChildren()).hasSize(1);  // 남성의류
    }

    @Test
    @DisplayName("카테고리 트리 조회 - 빈 리스트인 경우")
    void getCategoryTree_EmptyList() {
        // given
        given(loadCategoryPort.findAll()).willReturn(List.of());

        // when
        List<CategoryTreeNodeResult> result = categoryQueryService.getCategoryTree();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("카테고리 트리 조회 - 최상위 카테고리만 있는 경우")
    void getCategoryTree_OnlyRootCategories() {
        // given
        List<Category> rootOnly = List.of(
            createCategory(1L, "전자제품", null),
            createCategory(2L, "의류", null)
        );
        given(loadCategoryPort.findAll()).willReturn(rootOnly);

        // when
        List<CategoryTreeNodeResult> result = categoryQueryService.getCategoryTree();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(node -> node.getChildren().isEmpty());
    }

    @Test
    @DisplayName("카테고리 트리 조회 - 깊은 계층 구조 (5단계)")
    void getCategoryTree_DeepHierarchy() {
        // given
        // 1 -> 2 -> 3 -> 4 -> 5 (5단계 깊이)
        List<Category> deepCategories = Arrays.asList(
            createCategory(1L, "Level 1", null),
            createCategory(2L, "Level 2", 1L),
            createCategory(3L, "Level 3", 2L),
            createCategory(4L, "Level 4", 3L),
            createCategory(5L, "Level 5", 4L)
        );
        given(loadCategoryPort.findAll()).willReturn(deepCategories);

        // when
        List<CategoryTreeNodeResult> result = categoryQueryService.getCategoryTree();

        // then
        assertThat(result).hasSize(1);

        CategoryTreeNodeResult level1 = result.get(0);
        assertThat(level1.getName()).isEqualTo("Level 1");
        assertThat(level1.getChildren()).hasSize(1);

        CategoryTreeNodeResult level2 = level1.getChildren().get(0);
        assertThat(level2.getName()).isEqualTo("Level 2");
        assertThat(level2.getChildren()).hasSize(1);

        CategoryTreeNodeResult level3 = level2.getChildren().get(0);
        assertThat(level3.getName()).isEqualTo("Level 3");
        assertThat(level3.getChildren()).hasSize(1);

        CategoryTreeNodeResult level4 = level3.getChildren().get(0);
        assertThat(level4.getName()).isEqualTo("Level 4");
        assertThat(level4.getChildren()).hasSize(1);

        CategoryTreeNodeResult level5 = level4.getChildren().get(0);
        assertThat(level5.getName()).isEqualTo("Level 5");
        assertThat(level5.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("카테고리 트리 조회 - 성능 테스트 (100개 카테고리)")
    void getCategoryTree_PerformanceTest() {
        // given
        List<Category> manyCategories = new java.util.ArrayList<>();
        // 최상위 10개
        for (int i = 1; i <= 10; i++) {
            manyCategories.add(createCategory((long) i, "Root " + i, null));
        }
        // 각 최상위에 자식 9개씩 (총 90개)
        for (int root = 1; root <= 10; root++) {
            for (int child = 1; child <= 9; child++) {
                long id = root * 10 + child;
                manyCategories.add(createCategory(id, "Child " + id, (long) root));
            }
        }

        given(loadCategoryPort.findAll()).willReturn(manyCategories);

        // when
        long startTime = System.currentTimeMillis();
        List<CategoryTreeNodeResult> result = categoryQueryService.getCategoryTree();
        long endTime = System.currentTimeMillis();

        // then
        assertThat(result).hasSize(10);  // 최상위 10개
        result.forEach(root ->
            assertThat(root.getChildren()).hasSize(9)  // 각각 자식 9개
        );

        // 성능 검증: 100ms 이내 (메모리 연산이므로 매우 빠름)
        long executionTime = endTime - startTime;
        assertThat(executionTime).isLessThan(100L);
    }

    private Category createCategory(Long id, String name, Long parentId) {
        return Category.builder()
            .categoryId(id)
            .name(name)
            .description(name + " 설명")
            .parentCategoryId(parentId)
            .deleted(false)
            .build();
    }

    private CategoryTreeNodeResult findNodeById(List<CategoryTreeNodeResult> nodes, Long id) {
        return nodes.stream()
            .filter(node -> node.getCategoryId().equals(id))
            .findFirst()
            .orElse(null);
    }
}
