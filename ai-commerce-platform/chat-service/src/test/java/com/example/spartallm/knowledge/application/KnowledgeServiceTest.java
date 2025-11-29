package com.example.spartallm.knowledge.application;

import com.example.spartallm.knowledge.application.dto.result.CreateKnowledgeResult;
import com.example.spartallm.knowledge.domain.command.CreateKnowledgeCommand;
import com.example.spartallm.knowledge.domain.entity.AccessControl;
import com.example.spartallm.knowledge.domain.entity.Knowledge;
import com.example.spartallm.knowledge.domain.port.FakeKnowledgeCommandPort;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KnowledgeService 테스트")
class KnowledgeServiceTest {

    private KnowledgeService knowledgeService;
    private FakeKnowledgeCommandPort fakeKnowledgeCommandPort;

    @BeforeEach
    void setUp() {
        // given: 테스트 환경 구성
        fakeKnowledgeCommandPort = new FakeKnowledgeCommandPort();
        knowledgeService = new KnowledgeService(fakeKnowledgeCommandPort);
    }

    @AfterEach
    void tearDown() {
        fakeKnowledgeCommandPort.clear();
    }

    @Nested
    @DisplayName("createKnowledge 메서드는")
    class CreateKnowledgeTest {

        @Test
        @DisplayName("기본 정보로 Knowledge를 생성할 수 있다")
        void createKnowledgeWithBasicInfo() {
            // given: 기본 Knowledge 생성 명령
            Long userId = 1L;
            String name = "Spring Boot Guide";
            String description = "Spring Boot 학습 자료";
            AccessControl accessControl = AccessControl.createDefault();

            CreateKnowledgeCommand command = new CreateKnowledgeCommand(
                userId,
                name,
                description,
                accessControl
            );

            // when: Knowledge를 생성하면
            CreateKnowledgeResult result = knowledgeService.createKnowledge(command);

            // then: 생성된 Knowledge가 반환되고
            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.name()).isEqualTo(name);
            assertThat(result.description()).isEqualTo(description);
            assertThat(result.accessControl()).isNotNull();

            // then: 저장소에 Knowledge가 저장된다
            Knowledge savedKnowledge = fakeKnowledgeCommandPort.findById(result.id());
            assertThat(savedKnowledge).isNotNull();
            assertThat(savedKnowledge.getId()).isEqualTo(result.id());
            assertThat(savedKnowledge.getUserId()).isEqualTo(userId);
            assertThat(savedKnowledge.getName()).isEqualTo(name);
            assertThat(fakeKnowledgeCommandPort.size()).isEqualTo(1);
        }

        @ParameterizedTest(name = "[{index}] {3}")
        @MethodSource("provideKnowledgeCreationScenarios")
        @DisplayName("다양한 입력 조합으로 Knowledge를 생성할 수 있다")
        void createKnowledgeWithVariousInputs(
            Long userId,
            String name,
            String description,
            String scenario,
            AccessControl accessControl
        ) {
            // given: 다양한 입력 조합의 Knowledge 생성 명령
            CreateKnowledgeCommand command = new CreateKnowledgeCommand(
                userId,
                name,
                description,
                accessControl
            );

            // when: Knowledge를 생성하면
            CreateKnowledgeResult result = knowledgeService.createKnowledge(command);

            // then: 생성된 Knowledge가 올바르게 반환된다
            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.name()).isEqualTo(name);
            assertThat(result.description()).isEqualTo(description);

            // then: AccessControl이 null이면 기본값이 설정된다
            if (accessControl == null) {
                assertThat(result.accessControl()).isNotNull();
                assertThat(result.accessControl().getRead()).isNotNull();
                assertThat(result.accessControl().getWrite()).isNotNull();
            } else {
                assertThat(result.accessControl()).isEqualTo(accessControl);
            }

            // then: 저장소에 Knowledge가 저장된다
            Knowledge savedKnowledge = fakeKnowledgeCommandPort.findById(result.id());
            assertThat(savedKnowledge).isNotNull();
        }

        private static Stream<Arguments> provideKnowledgeCreationScenarios() {
            AccessControl customAccessControl = AccessControl.builder()
                .read(AccessControl.AccessPermission.builder()
                    .userIds(List.of(1L, 2L, 3L))
                    .groupIds(List.of(10L))
                    .build())
                .write(AccessControl.AccessPermission.builder()
                    .userIds(List.of(1L))
                    .build())
                .build();

            return Stream.of(
                Arguments.of(
                    1L,
                    "Java Programming",
                    "Java 프로그래밍 기초",
                    "설명이 있는 Knowledge",
                    AccessControl.createDefault()
                ),
                Arguments.of(
                    2L,
                    "Design Patterns",
                    null,
                    "설명이 null인 Knowledge",
                    AccessControl.createDefault()
                ),
                Arguments.of(
                    3L,
                    "Microservices",
                    "",
                    "설명이 빈 문자열인 Knowledge",
                    null
                ),
                Arguments.of(
                    4L,
                    "Clean Code",
                    "클린 코드 작성법",
                    "AccessControl이 null인 Knowledge (기본값 사용)",
                    null
                ),
                Arguments.of(
                    5L,
                    "REST API Design",
                    "RESTful API 설계 가이드",
                    "커스텀 AccessControl을 가진 Knowledge",
                    customAccessControl
                )
            );
        }

        @Test
        @DisplayName("여러 Knowledge를 순차적으로 생성할 수 있다")
        void createMultipleKnowledges() {
            // given: 3개의 Knowledge 생성 명령
            CreateKnowledgeCommand command1 = new CreateKnowledgeCommand(
                1L,
                "Knowledge 1",
                "첫 번째 Knowledge",
                null
            );
            CreateKnowledgeCommand command2 = new CreateKnowledgeCommand(
                2L,
                "Knowledge 2",
                "두 번째 Knowledge",
                null
            );
            CreateKnowledgeCommand command3 = new CreateKnowledgeCommand(
                3L,
                "Knowledge 3",
                "세 번째 Knowledge",
                null
            );

            // when: 3개의 Knowledge를 순차적으로 생성하면
            CreateKnowledgeResult result1 = knowledgeService.createKnowledge(command1);
            CreateKnowledgeResult result2 = knowledgeService.createKnowledge(command2);
            CreateKnowledgeResult result3 = knowledgeService.createKnowledge(command3);

            // then: 모든 Knowledge가 정상적으로 생성되고
            assertThat(result1.id()).isNotNull();
            assertThat(result2.id()).isNotNull();
            assertThat(result3.id()).isNotNull();

            // then: 각 Knowledge는 고유한 ID를 가지며
            assertThat(result1.id()).isNotEqualTo(result2.id());
            assertThat(result2.id()).isNotEqualTo(result3.id());
            assertThat(result3.id()).isNotEqualTo(result1.id());

            // then: 저장소에 3개의 Knowledge가 저장된다
            assertThat(fakeKnowledgeCommandPort.size()).isEqualTo(3);
            assertThat(fakeKnowledgeCommandPort.findById(result1.id())).isNotNull();
            assertThat(fakeKnowledgeCommandPort.findById(result2.id())).isNotNull();
            assertThat(fakeKnowledgeCommandPort.findById(result3.id())).isNotNull();
        }

        @Test
        @DisplayName("생성된 Knowledge는 올바른 도메인 속성을 가진다")
        void createdKnowledgeHasCorrectDomainProperties() {
            // given: Knowledge 생성 명령
            Long userId = 999L;
            String name = "Domain Model";
            String description = "DDD Domain Model 설계";
            AccessControl accessControl = AccessControl.createDefault();

            CreateKnowledgeCommand command = new CreateKnowledgeCommand(
                userId,
                name,
                description,
                accessControl
            );

            // when: Knowledge를 생성하면
            CreateKnowledgeResult result = knowledgeService.createKnowledge(command);

            // then: 생성된 Knowledge는 올바른 도메인 속성을 가진다
            Knowledge savedKnowledge = fakeKnowledgeCommandPort.findById(result.id());

            assertThat(savedKnowledge.getId()).isNotNull();
            assertThat(savedKnowledge.getUserId()).isEqualTo(userId);
            assertThat(savedKnowledge.getName()).isEqualTo(name);
            assertThat(savedKnowledge.getDescription()).isEqualTo(description);
            assertThat(savedKnowledge.getAccessControl()).isNotNull();
            assertThat(savedKnowledge.getMeta()).isNotNull();
            assertThat(savedKnowledge.getData()).isNull(); // 초기 생성 시 data는 null
        }

        @Test
        @DisplayName("같은 사용자가 여러 Knowledge를 생성할 수 있다")
        void sameUserCanCreateMultipleKnowledges() {
            // given: 같은 사용자의 여러 Knowledge 생성 명령
            Long userId = 100L;

            CreateKnowledgeCommand command1 = new CreateKnowledgeCommand(
                userId,
                "Knowledge A",
                "첫 번째",
                null
            );
            CreateKnowledgeCommand command2 = new CreateKnowledgeCommand(
                userId,
                "Knowledge B",
                "두 번째",
                null
            );

            // when: 같은 사용자가 2개의 Knowledge를 생성하면
            CreateKnowledgeResult result1 = knowledgeService.createKnowledge(command1);
            CreateKnowledgeResult result2 = knowledgeService.createKnowledge(command2);

            // then: 두 Knowledge 모두 정상적으로 생성되고
            assertThat(result1.userId()).isEqualTo(userId);
            assertThat(result2.userId()).isEqualTo(userId);

            // then: 각각 고유한 ID를 가지며
            assertThat(result1.id()).isNotEqualTo(result2.id());

            // then: 저장소에 2개의 Knowledge가 저장된다
            assertThat(fakeKnowledgeCommandPort.size()).isEqualTo(2);
        }
    }
}