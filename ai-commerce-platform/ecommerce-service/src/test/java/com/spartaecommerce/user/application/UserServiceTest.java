package com.spartaecommerce.user.application;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.application.dto.command.UserCreateCommand;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.repository.UserFakeRepository;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.port.out.PointWalletFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserService")
class UserServiceTest {

    private UserService sut;
    private UserFakeRepository userRepository;
    private PointWalletFakeRepository walletRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserFakeRepository();
        walletRepository = new PointWalletFakeRepository();
        sut = new UserService(userRepository, walletRepository, walletRepository);
    }

    @AfterEach
    void tearDown() {
        walletRepository.clear();
        userRepository.clear();
    }

    @Test
    @DisplayName("이메일 중복 시 BusinessException이 발생한다")
    void create_WithDuplicateEmail_ThrowsBusinessException() {
        // given
        String duplicateEmail = "test@example.com";

        UserCreateCommand firstCommand = new UserCreateCommand(
            duplicateEmail,
            "John Doe",
            "010-1234-5678"
        );

        UserCreateCommand secondCommand = new UserCreateCommand(
            duplicateEmail,
            "Jane Doe",
            "010-9876-5432"
        );

        // when
        sut.create(firstCommand);

        // then
        assertThatThrownBy(() -> sut.create(secondCommand))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.ENTITY_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("사용자 생성 시 지갑도 함께 생성된다")
    void create_WithValidRequest_CreatesUserAndWallet() {
        // given
        UserCreateCommand command = new UserCreateCommand(
            "test@example.com",
            "John Doe",
            "010-1234-5678"
        );

        // when
        Long userId = sut.create(command);

        // then
        assertThat(userId).isNotNull();

        User savedUser = userRepository.getById(userId);
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(savedUser.isDeleted()).isFalse();

        PointWallet savedWallet = walletRepository.getByUserId(userId);
        assertThat(savedWallet).isNotNull();
        assertThat(savedWallet.getUserId()).isEqualTo(userId);
        assertThat(savedWallet.isActive()).isTrue();
    }

    @Test
    @DisplayName("사용자 삭제 시 지갑의 active 플래그가 false로 변경된다")
    void delete_User_DeactivatesWallet() {
        // given
        UserCreateCommand command = new UserCreateCommand(
            "test@example.com",
            "John Doe",
            "010-1234-5678"
        );

        Long userId = sut.create(command);

        // when
        sut.delete(userId);

        // then
        User deletedUser = userRepository.findById(userId).orElse(null);
        assertThat(deletedUser).isNull(); // soft delete로 인해 조회 불가

        PointWallet wallet = walletRepository.getByUserId(userId);
        assertThat(wallet.isActive()).isFalse();
    }

    @Test
    @DisplayName("이미 삭제된 사용자를 다시 삭제하려 하면 BusinessException이 발생한다")
    void delete_AlreadyDeletedUser_ThrowsBusinessException() {
        // given
        UserCreateCommand command = new UserCreateCommand(
            "test@example.com",
            "John Doe",
            "010-1234-5678"
        );

        Long userId = sut.create(command);
        sut.delete(userId);

        // when & then
        assertThatThrownBy(() -> sut.delete(userId))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}
