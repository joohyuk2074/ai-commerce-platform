package com.spartaecommerce.pointwallet.application.service;

import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.common.util.DateTimeHolder;
import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.application.dto.command.EarnPointCommand;
import com.spartaecommerce.pointwallet.application.dto.command.UsePointCommand;
import com.spartaecommerce.pointwallet.domain.entity.PointTransactionType;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.port.out.PointTransactionFakeRepository;
import com.spartaecommerce.pointwallet.domain.port.out.PointWalletFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PointWalletCommandService")
class PointWalletCommandServiceTest {

    private PointWalletCommandService sut;
    private PointWalletFakeRepository walletRepository;
    private PointTransactionFakeRepository transactionRepository;
    private PointsProperties pointsProperties;
    private TestDateTimeHolder dateTimeHolder;

    @BeforeEach
    void setUp() {
        walletRepository = new PointWalletFakeRepository();
        transactionRepository = new PointTransactionFakeRepository();
        pointsProperties = createPointsProperties();
        dateTimeHolder = new TestDateTimeHolder();

        sut = new PointWalletCommandService(
            walletRepository,
            walletRepository,
            transactionRepository,
            transactionRepository,
            pointsProperties,
            dateTimeHolder
        );
    }

    @AfterEach
    void tearDown() {
        walletRepository.clear();
        transactionRepository.clear();
    }

    @Nested
    @DisplayName("포인트 적립 시")
    class EarnPoints {

        @Test
        @DisplayName("새로운 사용자가 포인트를 적립한다")
        void earnPoints_NewUser_CreatesWalletAndEarnsPoints() {
            // given
            Long userId = 1L;
            Money amount = Money.from(new BigDecimal("1000"));
            EarnPointCommand command = new EarnPointCommand(userId, amount, "첫 구매 적립");

            // when
            PointTransactionResult result = sut.earnPoints(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(PointTransactionType.EARN);
            assertThat(result.amount()).isEqualTo(amount);
            assertThat(result.balanceAfter()).isEqualTo(amount);
            assertThat(result.description()).isEqualTo("첫 구매 적립");

            PointWallet wallet = walletRepository.getByUserId(userId);
            assertThat(wallet.getBalance()).isEqualTo(amount);
        }

        @Test
        @DisplayName("기존 사용자가 포인트를 추가 적립한다")
        void earnPoints_ExistingUser_AddsPoints() {
            // given
            Long userId = 1L;
            Money initialAmount = Money.from(new BigDecimal("1000"));
            Money additionalAmount = Money.from(new BigDecimal("500"));

            // 초기 적립
            sut.earnPoints(new EarnPointCommand(userId, initialAmount, "첫 구매"));

            // when
            EarnPointCommand command = new EarnPointCommand(userId, additionalAmount, "두번째 구매");
            PointTransactionResult result = sut.earnPoints(command);

            // then
            assertThat(result.balanceAfter()).isEqualTo(Money.from(new BigDecimal("1500")));

            PointWallet wallet = walletRepository.getByUserId(userId);
            assertThat(wallet.getBalance()).isEqualTo(Money.from(new BigDecimal("1500")));
        }

        @Test
        @DisplayName("최대 잔액을 초과하면 예외가 발생한다")
        void earnPoints_ExceedsMaxBalance_ThrowsException() {
            // given
            Long userId = 1L;
            Money maxBalance = Money.from(pointsProperties.getMaxBalance());
            Money excessAmount = maxBalance.add(Money.from(new BigDecimal("1")));

            EarnPointCommand command = new EarnPointCommand(userId, excessAmount, "초과 적립");

            // when & then
            assertThatThrownBy(() -> sut.earnPoints(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }
    }

    @Nested
    @DisplayName("포인트 사용 시")
    class UsePoints {

        @Test
        @DisplayName("보유 포인트 범위 내에서 포인트를 사용한다")
        void usePoints_WithinBalance_UsesPoints() {
            // given
            Long userId = 1L;
            Money earnAmount = Money.from(new BigDecimal("5000"));
            Money useAmount = Money.from(new BigDecimal("3000"));

            // 먼저 적립
            sut.earnPoints(new EarnPointCommand(userId, earnAmount, "적립"));

            // when
            UsePointCommand command = new UsePointCommand(userId, useAmount, 10000, "주문 결제");
            PointTransactionResult result = sut.usePoints(command);

            // then
            assertThat(result.type()).isEqualTo(PointTransactionType.USE);
            assertThat(result.amount()).isEqualTo(useAmount);
            assertThat(result.balanceAfter()).isEqualTo(Money.from(new BigDecimal("2000")));

            PointWallet wallet = walletRepository.getByUserId(userId);
            assertThat(wallet.getBalance()).isEqualTo(Money.from(new BigDecimal("2000")));
        }

        @Test
        @DisplayName("잔액이 부족하면 예외가 발생한다")
        void usePoints_InsufficientBalance_ThrowsException() {
            // given
            Long userId = 1L;
            Money earnAmount = Money.from(new BigDecimal("1000"));
            Money useAmount = Money.from(new BigDecimal("2000"));

            // 먼저 적립
            sut.earnPoints(new EarnPointCommand(userId, earnAmount, "적립"));

            // when & then
            UsePointCommand command = new UsePointCommand(userId, useAmount, 10000, "주문 결제");
            assertThatThrownBy(() -> sut.usePoints(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }

        @Test
        @DisplayName("최소 사용 포인트보다 적으면 예외가 발생한다")
        void usePoints_BelowMinUsage_ThrowsException() {
            // given
            Long userId = 1L;
            Money earnAmount = Money.from(new BigDecimal("5000"));
            Money useAmount = Money.from(new BigDecimal("500")); // 최소 사용 포인트(1000)보다 적음

            // 먼저 적립
            sut.earnPoints(new EarnPointCommand(userId, earnAmount, "적립"));

            // when & then
            UsePointCommand command = new UsePointCommand(userId, useAmount, 10000, "주문 결제");
            assertThatThrownBy(() -> sut.usePoints(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }

        @Test
        @DisplayName("최소 주문 금액보다 적으면 예외가 발생한다")
        void usePoints_BelowMinOrderAmount_ThrowsException() {
            // given
            Long userId = 1L;
            Money earnAmount = Money.from(new BigDecimal("5000"));
            Money useAmount = Money.from(new BigDecimal("3000"));

            // 먼저 적립
            sut.earnPoints(new EarnPointCommand(userId, earnAmount, "적립"));

            // when & then
            UsePointCommand command = new UsePointCommand(userId, useAmount, 5000, "주문 결제"); // 최소 주문 금액(10000)보다 적음
            assertThatThrownBy(() -> sut.usePoints(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        }
    }

    private PointsProperties createPointsProperties() {
        PointsProperties properties = new PointsProperties();
        properties.setMaxBalance(new BigDecimal("100000"));
        properties.setMinUsagePoint(1000);
        properties.setMinUsageAmount(10000);
        properties.setExpireDays(30);
        return properties;
    }

    private static class TestDateTimeHolder implements DateTimeHolder {
        private LocalDateTime currentDateTime = LocalDateTime.now();

        @Override
        public LocalDateTime getCurrentDateTime() {
            return currentDateTime;
        }

        public void setCurrentDateTime(LocalDateTime dateTime) {
            this.currentDateTime = dateTime;
        }
    }
}
