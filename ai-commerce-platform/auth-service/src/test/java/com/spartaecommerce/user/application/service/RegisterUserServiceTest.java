package com.spartaecommerce.user.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.entity.UserGrade;
import com.spartaecommerce.user.domain.event.UserCreatedEvent;
import com.spartaecommerce.user.fixture.FakeLoadUserPort;
import com.spartaecommerce.user.fixture.FakePasswordEncoderPort;
import com.spartaecommerce.user.fixture.FakeSaveUserPort;
import com.spartaecommerce.user.fixture.FakeUserEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.spartaecommerce.user.fixture.TestDataBuilder.aRegisterUserCommand;
import static com.spartaecommerce.user.fixture.TestDataBuilder.aUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RegisterUserService 단위 테스트
 * <p>
 * Classical Testing Style (Detroit School):
 * - Fake implementations instead of mocks
 * - State-based verification (not interaction-based)
 * - Tests verify observable outcomes
 * - High refactoring resistance
 * <p>
 * Big Tech Testing Practices:
 * - Clear test structure: Given-When-Then
 * - Descriptive test names that explain business scenarios
 * - Comprehensive edge case coverage
 * - Test data builders for readable setup
 * - Nested test classes for logical grouping
 * - Fast execution (no external dependencies)
 */
@DisplayName("RegisterUserService")
class RegisterUserServiceTest {

    private FakeLoadUserPort loadUserPort;
    private FakeSaveUserPort saveUserPort;
    private FakePasswordEncoderPort passwordEncoder;
    private FakeUserEventPublisher eventPublisher;
    private RegisterUserService sut; // System Under Test

    @BeforeEach
    void setUp() {
        loadUserPort = new FakeLoadUserPort();
        saveUserPort = new FakeSaveUserPort(loadUserPort);
        passwordEncoder = new FakePasswordEncoderPort();
        eventPublisher = new FakeUserEventPublisher();

        sut = new RegisterUserService(
            loadUserPort,
            saveUserPort,
            passwordEncoder,
            eventPublisher
        );
    }

    @Nested
    @DisplayName("사용자 등록 성공 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("정상적인 정보로 사용자를 등록하면, User가 생성되고 UserCreatedEvent가 발행된다")
        void register_WithValidInput_CreatesUserAndPublishesEvent() {
            // Given - 신규 사용자 등록 요청
            RegisterUserCommand command = aRegisterUserCommand()
                .withUsername("newuser")
                .withPassword("password123")
                .withEmail("newuser@example.com")
                .withName("신규사용자")
                .withPhoneNumber("01012345678")
                .build();

            // When - 사용자 등록 실행
            Long userId = sut.register(command);

            // Then - User가 올바르게 생성되었는지 검증
            assertThat(userId).isNotNull().isPositive();

            User savedUser = loadUserPort.findById(userId).orElseThrow();
            assertThat(savedUser.getUserId()).isEqualTo(userId);
            assertThat(savedUser.getUsername()).isEqualTo("newuser");
            assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
            assertThat(savedUser.getName()).isEqualTo("신규사용자");
            assertThat(savedUser.getPhoneNumber()).isEqualTo("01012345678");
            assertThat(savedUser.getGrade()).isEqualTo(UserGrade.NORMAL);
            assertThat(savedUser.isDeleted()).isFalse();

            // Then - 비밀번호가 암호화되었는지 검증
            assertThat(savedUser.getPassword()).isNotEqualTo("password123");
            assertThat(savedUser.getPassword()).startsWith("encoded_");
            assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();

            // Then - UserCreatedEvent가 발행되었는지 검증
            assertThat(eventPublisher.getPublishedEventCount()).isEqualTo(1);

            UserCreatedEvent publishedEvent = eventPublisher.getLastPublishedEvent();
            assertThat(publishedEvent).isNotNull();
            assertThat(publishedEvent.getUserId()).isEqualTo(userId);
            assertThat(publishedEvent.getUsername()).isEqualTo("newuser");
            assertThat(publishedEvent.getEmail()).isEqualTo("newuser@example.com");
            assertThat(publishedEvent.getName()).isEqualTo("신규사용자");
            assertThat(publishedEvent.getPhoneNumber()).isEqualTo("01012345678");
            assertThat(publishedEvent.getGrade()).isEqualTo("NORMAL");
            assertThat(publishedEvent.getEventType()).isEqualTo("UserCreatedEvent");
            assertThat(publishedEvent.getEventId()).isNotNull();
            assertThat(publishedEvent.getOccurredAt()).isNotNull();
        }

        @Test
        @DisplayName("여러 사용자를 연속으로 등록하면, 각각 고유한 ID를 받고 모든 이벤트가 발행된다")
        void register_MultipleUsers_AssignsUniqueIdsAndPublishesAllEvents() {
            // Given - 3명의 신규 사용자 등록 요청
            RegisterUserCommand command1 = aRegisterUserCommand()
                .withUsername("user1")
                .withEmail("user1@example.com")
                .build();

            RegisterUserCommand command2 = aRegisterUserCommand()
                .withUsername("user2")
                .withEmail("user2@example.com")
                .build();

            RegisterUserCommand command3 = aRegisterUserCommand()
                .withUsername("user3")
                .withEmail("user3@example.com")
                .build();

            // When - 3명의 사용자 등록
            Long userId1 = sut.register(command1);
            Long userId2 = sut.register(command2);
            Long userId3 = sut.register(command3);

            // Then - 각 사용자가 고유한 ID를 받았는지 검증
            assertThat(userId1).isNotEqualTo(userId2);
            assertThat(userId2).isNotEqualTo(userId3);
            assertThat(userId1).isNotEqualTo(userId3);

            // Then - 모든 사용자가 저장되었는지 검증
            assertThat(loadUserPort.findById(userId1)).isPresent();
            assertThat(loadUserPort.findById(userId2)).isPresent();
            assertThat(loadUserPort.findById(userId3)).isPresent();

            // Then - 3개의 이벤트가 모두 발행되었는지 검증
            assertThat(eventPublisher.getPublishedEventCount()).isEqualTo(3);
            assertThat(eventPublisher.hasEventForUser(userId1)).isTrue();
            assertThat(eventPublisher.hasEventForUser(userId2)).isTrue();
            assertThat(eventPublisher.hasEventForUser(userId3)).isTrue();
        }
    }

    @Nested
    @DisplayName("사용자 등록 실패 시나리오 - 중복 검증")
    class DuplicateValidationFailureScenarios {

        @Test
        @DisplayName("이미 존재하는 username으로 등록 시도하면, BusinessException이 발생하고 이벤트가 발행되지 않는다")
        void register_WithDuplicateUsername_ThrowsExceptionAndDoesNotPublishEvent() {
            // Given - 기존 사용자가 이미 존재
            User existingUser = aUser()
                .withUsername("duplicateuser")
                .withEmail("existing@example.com")
                .build();
            saveUserPort.save(existingUser);

            // Given - 동일한 username으로 새 사용자 등록 시도
            RegisterUserCommand command = aRegisterUserCommand()
                .withUsername("duplicateuser") // 중복
                .withEmail("newemail@example.com")
                .build();

            // When & Then - BusinessException 발생
            assertThatThrownBy(() -> sut.register(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_ALREADY_EXISTS)
                .hasMessageContaining("Username already exists: duplicateuser");

            // Then - 이벤트가 발행되지 않았는지 검증
            assertThat(eventPublisher.getPublishedEventCount()).isZero();
        }

        @Test
        @DisplayName("이미 존재하는 email로 등록 시도하면, BusinessException이 발생하고 이벤트가 발행되지 않는다")
        void register_WithDuplicateEmail_ThrowsExceptionAndDoesNotPublishEvent() {
            // Given - 기존 사용자가 이미 존재
            User existingUser = aUser()
                .withUsername("existinguser")
                .withEmail("duplicate@example.com")
                .build();
            saveUserPort.save(existingUser);

            // Given - 동일한 email로 새 사용자 등록 시도
            RegisterUserCommand command = aRegisterUserCommand()
                .withUsername("newuser")
                .withEmail("duplicate@example.com") // 중복
                .build();

            // When & Then - BusinessException 발생
            assertThatThrownBy(() -> sut.register(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_ALREADY_EXISTS)
                .hasMessageContaining("Email already exists: duplicate@example.com");

            // Then - 이벤트가 발행되지 않았는지 검증
            assertThat(eventPublisher.getPublishedEventCount()).isZero();
        }

        @ParameterizedTest(name = "[{index}] {4}")
        @MethodSource("provideDuplicateTestCases")
        @DisplayName("중복된 username 또는 email의 다양한 조합에서 BusinessException이 발생한다")
        void register_WithDuplicates_ThrowsException(
            String existingUsername,
            String existingEmail,
            String newUsername,
            String newEmail,
            String testDescription
        ) {
            // Given - 기존 사용자 등록
            User existingUser = aUser()
                .withUsername(existingUsername)
                .withEmail(existingEmail)
                .build();
            saveUserPort.save(existingUser);

            RegisterUserCommand command = aRegisterUserCommand()
                .withUsername(newUsername)
                .withEmail(newEmail)
                .build();

            // When & Then - BusinessException 발생
            assertThatThrownBy(() -> sut.register(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_ALREADY_EXISTS);

            // Then - 이벤트가 발행되지 않았는지 검증
            assertThat(eventPublisher.getPublishedEventCount()).isZero();
        }

        private static Stream<Arguments> provideDuplicateTestCases() {
            return Stream.of(
                Arguments.of(
                    "duplicate", "existing@example.com",
                    "duplicate", "new@example.com",
                    "username만 중복"
                ),
                Arguments.of(
                    "existing", "duplicate@example.com",
                    "newuser", "duplicate@example.com",
                    "email만 중복"
                ),
                Arguments.of(
                    "duplicate", "duplicate@example.com",
                    "duplicate", "duplicate@example.com",
                    "username과 email 모두 중복"
                )
            );
        }
    }

    @Nested
    @DisplayName("비밀번호 암호화 검증")
    class PasswordEncodingScenarios {

        @Test
        @DisplayName("사용자 등록 시 비밀번호는 항상 암호화되어 저장된다")
        void register_AlwaysEncryptPassword() {
            // Given
            String rawPassword = "mySecretPassword123!";
            RegisterUserCommand command = aRegisterUserCommand()
                .withPassword(rawPassword)
                .build();

            // When
            Long userId = sut.register(command);

            // Then - 원본 비밀번호와 다른 암호화된 비밀번호가 저장됨
            User savedUser = loadUserPort.findById(userId).orElseThrow();
            assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword);
            assertThat(savedUser.getPassword()).startsWith("encoded_");

            // Then - PasswordEncoder로 검증 가능
            assertThat(passwordEncoder.matches(rawPassword, savedUser.getPassword())).isTrue();
            assertThat(passwordEncoder.matches("wrongPassword", savedUser.getPassword())).isFalse();
        }

        @ParameterizedTest(name = "[{index}] 비밀번호: {0}")
        @MethodSource("provideVariousPasswords")
        @DisplayName("다양한 형식의 비밀번호가 모두 올바르게 암호화된다")
        void register_WithVariousPasswords_EncryptsCorrectly(String password) {
            // Given
            RegisterUserCommand command = aRegisterUserCommand()
                .withUsername("user_" + password.hashCode()) // 고유한 username
                .withEmail("user_" + password.hashCode() + "@example.com") // 고유한 email
                .withPassword(password)
                .build();

            // When
            Long userId = sut.register(command);

            // Then
            User savedUser = loadUserPort.findById(userId).orElseThrow();
            assertThat(savedUser.getPassword()).isNotEqualTo(password);
            assertThat(passwordEncoder.matches(password, savedUser.getPassword())).isTrue();
        }

        private static Stream<Arguments> provideVariousPasswords() {
            return Stream.of(
                Arguments.of("simple"),
                Arguments.of("Complex123!"),
                Arguments.of("한글비밀번호123"),
                Arguments.of("with spaces in password"),
                Arguments.of("!@#$%^&*()_+-=[]{}|;':\",./<>?"),
                Arguments.of("a".repeat(100)) // 긴 비밀번호
            );
        }
    }

    @Nested
    @DisplayName("이벤트 발행 검증")
    class EventPublishingScenarios {

        @Test
        @DisplayName("등록 성공 시 UserCreatedEvent의 모든 필드가 올바르게 설정된다")
        void register_PublishesEventWithCorrectFields() {
            // Given
            RegisterUserCommand command = aRegisterUserCommand()
                .withUsername("eventuser")
                .withEmail("eventuser@example.com")
                .withName("이벤트테스트")
                .withPhoneNumber("01099998888")
                .build();

            // When
            Long userId = sut.register(command);

            // Then - Event 필드 상세 검증
            UserCreatedEvent event = eventPublisher.getLastPublishedEvent();
            assertThat(event).isNotNull();

            // 비즈니스 필드 검증
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getUsername()).isEqualTo("eventuser");
            assertThat(event.getEmail()).isEqualTo("eventuser@example.com");
            assertThat(event.getName()).isEqualTo("이벤트테스트");
            assertThat(event.getPhoneNumber()).isEqualTo("01099998888");
            assertThat(event.getGrade()).isEqualTo(UserGrade.NORMAL.name());

            // 메타 필드 검증
            assertThat(event.getEventType()).isEqualTo("UserCreatedEvent");
            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getOccurredAt()).isNotNull();
            assertThat(event.getSchemaVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("등록 실패 시 UserCreatedEvent가 발행되지 않는다")
        void register_OnFailure_DoesNotPublishEvent() {
            // Given - 중복 username으로 실패 상황 설정
            User existingUser = aUser()
                .withUsername("existing")
                .build();
            saveUserPort.save(existingUser);

            RegisterUserCommand command = aRegisterUserCommand()
                .withUsername("existing") // 중복
                .build();

            // When - 등록 시도 (실패)
            try {
                sut.register(command);
            } catch (BusinessException e) {
                // 예외는 예상된 동작
            }

            // Then - 이벤트가 발행되지 않음
            assertThat(eventPublisher.getPublishedEventCount()).isZero();
            assertThat(eventPublisher.getLastPublishedEvent()).isNull();
        }
    }

    @Nested
    @DisplayName("엣지 케이스 및 경계값 테스트")
    class EdgeCaseScenarios {

        @Test
        @DisplayName("최소한의 유효한 정보로도 사용자 등록이 성공한다")
        void register_WithMinimalValidInput_Succeeds() {
            // Given - 필수 필드만 제공
            RegisterUserCommand command = aRegisterUserCommand()
                .withUsername("a") // 최소 길이
                .withPassword("p") // 최소 길이
                .withEmail("a@b.c") // 최소 유효 이메일
                .withName("가")
                .withPhoneNumber("01012345678")
                .build();

            // When
            Long userId = sut.register(command);

            // Then
            assertThat(userId).isNotNull();
            User savedUser = loadUserPort.findById(userId).orElseThrow();
            assertThat(savedUser.getUsername()).isEqualTo("a");
            assertThat(eventPublisher.getPublishedEventCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("긴 문자열 입력도 정상적으로 처리된다")
        void register_WithLongStrings_Succeeds() {
            // Given - 매우 긴 문자열
            String longUsername = "u".repeat(50);
            String longEmail = "e".repeat(30) + "@example.com";
            String longName = "이".repeat(50);

            RegisterUserCommand command = aRegisterUserCommand()
                .withUsername(longUsername)
                .withEmail(longEmail)
                .withName(longName)
                .build();

            // When
            Long userId = sut.register(command);

            // Then
            User savedUser = loadUserPort.findById(userId).orElseThrow();
            assertThat(savedUser.getUsername()).hasSize(50);
            assertThat(savedUser.getName()).hasSize(50);
        }

        @Test
        @DisplayName("특수문자가 포함된 이메일도 정상적으로 처리된다")
        void register_WithSpecialCharactersInEmail_Succeeds() {
            // Given
            RegisterUserCommand command = aRegisterUserCommand()
                .withEmail("user+tag@sub.example.co.kr")
                .build();

            // When
            Long userId = sut.register(command);

            // Then
            User savedUser = loadUserPort.findById(userId).orElseThrow();
            assertThat(savedUser.getEmail()).isEqualTo("user+tag@sub.example.co.kr");

            UserCreatedEvent event = eventPublisher.getLastPublishedEvent();
            assertThat(event.getEmail()).isEqualTo("user+tag@sub.example.co.kr");
        }
    }
}
