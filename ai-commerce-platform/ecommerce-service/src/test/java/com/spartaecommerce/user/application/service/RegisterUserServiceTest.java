package com.spartaecommerce.user.application.service;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.fixture.FakeSavePointWalletPort;
import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.entity.UserGrade;
import com.spartaecommerce.user.fixture.FakeLoadUserPort;
import com.spartaecommerce.user.fixture.FakePasswordEncoderPort;
import com.spartaecommerce.user.fixture.FakeSaveUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserServiceTest {

    private RegisterUserService registerUserService;
    private FakeLoadUserPort loadUserPort;
    private FakeSaveUserPort saveUserPort;
    private FakePasswordEncoderPort passwordEncoderPort;
    private FakeSavePointWalletPort savePointWalletPort;

    @BeforeEach
    void setUp() {
        loadUserPort = new FakeLoadUserPort();
        saveUserPort = new FakeSaveUserPort(loadUserPort);
        passwordEncoderPort = new FakePasswordEncoderPort();
        savePointWalletPort = new FakeSavePointWalletPort();

        registerUserService = new RegisterUserService(
            loadUserPort,
            saveUserPort,
            passwordEncoderPort,
            savePointWalletPort
        );
    }

    @Test
    @DisplayName("정상적으로 사용자를 등록하면 User와 PointWallet이 생성된다")
    void register_Success() {
        // given
        RegisterUserCommand command = new RegisterUserCommand(
            "testuser",
            "password123",
            "test@example.com",
            "홍길동",
            "01012345678"
        );

        // when
        Long userId = registerUserService.register(command);

        // then
        assertThat(userId).isNotNull();

        // User가 저장되었는지 검증
        User savedUser = loadUserPort.findById(userId).orElseThrow();
        assertThat(savedUser.getUserId()).isEqualTo(userId);
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getPassword()).isEqualTo("encoded_password123");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(savedUser.getGrade()).isEqualTo(UserGrade.NORMAL);
        assertThat(savedUser.isDeleted()).isFalse();

        // PointWallet이 생성되었는지 검증
        PointWallet savedWallet = savePointWalletPort.findByUserId(userId).orElseThrow();
        assertThat(savedWallet.getUserId()).isEqualTo(userId);
        assertThat(savedWallet.getBalance()).isEqualTo(Money.ZERO);
        assertThat(savedWallet.isActive()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideDuplicateTestCases")
    @DisplayName("중복된 username 또는 email로 등록 시도하면 BusinessException이 발생한다")
    void register_DuplicateCheck_ThrowsException(
        String existingUsername,
        String existingEmail,
        String newUsername,
        String newEmail,
        String expectedErrorMessage
    ) {
        // given - 기존 사용자 등록
        User existingUser = User.builder()
            .userId(1L)
            .username(existingUsername)
            .password("encoded_password")
            .email(existingEmail)
            .name("기존유저")
            .phoneNumber("01011111111")
            .grade(UserGrade.NORMAL)
            .deleted(false)
            .build();
        saveUserPort.save(existingUser);

        RegisterUserCommand command = new RegisterUserCommand(
            newUsername,
            "password123",
            newEmail,
            "신규유저",
            "01022222222"
        );

        // when & then
        assertThatThrownBy(() -> registerUserService.register(command))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_ALREADY_EXISTS)
            .hasMessageContaining(expectedErrorMessage);

        // PointWallet이 생성되지 않았는지 검증 (롤백되어야 함)
        assertThat(savePointWalletPort.getAllWallets()).isEmpty();
    }

    private static Stream<Arguments> provideDuplicateTestCases() {
        return Stream.of(
            Arguments.of(
                "duplicate",
                "existing@example.com",
                "duplicate",
                "new@example.com",
                "Username already exists: duplicate"
            ),
            Arguments.of(
                "existing",
                "duplicate@example.com",
                "newuser",
                "duplicate@example.com",
                "Email already exists: duplicate@example.com"
            )
        );
    }
}
