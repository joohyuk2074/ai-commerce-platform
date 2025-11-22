package com.spartaecommerce.wallet.application;

import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.common.util.FakeDateTimeHolder;
import com.spartaecommerce.pointwallet.application.PointWalletService;
import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.application.dto.result.PointWalletResult;
import com.spartaecommerce.pointwallet.domain.command.EarnPointCommand;
import com.spartaecommerce.pointwallet.domain.command.UsePointCommand;
import com.spartaecommerce.pointwallet.domain.entity.PointTransactionType;
import com.spartaecommerce.wallet.domain.repository.PointTransactionFakeRepository;
import com.spartaecommerce.wallet.domain.repository.PointWalletFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PointWalletService")
class PointWalletServiceTest {

    private PointWalletService sut;
    private PointWalletFakeRepository pointWalletRepository;
    private PointTransactionFakeRepository pointTransactionRepository;
    private PointsProperties pointsProperties;
    private FakeDateTimeHolder dateTimeHolder;

    @BeforeEach
    void setUp() {
        pointWalletRepository = new PointWalletFakeRepository();
        pointTransactionRepository = new PointTransactionFakeRepository();
        dateTimeHolder = new FakeDateTimeHolder(LocalDateTime.of(2024, 1, 1, 0, 0));

        // PointsProperties 설정
        pointsProperties = new PointsProperties();
        pointsProperties.setMinUsageAmount(10000);
        pointsProperties.setMinUsagePoint(1000);
        pointsProperties.setExpireDays(30);
        pointsProperties.setMaxBalance(BigDecimal.valueOf(1000000L));

        sut = new PointWalletService(pointWalletRepository, pointTransactionRepository, pointsProperties, dateTimeHolder);
    }

    @AfterEach
    void tearDown() {
        pointWalletRepository.clear();
        pointTransactionRepository.clear();
    }

    @Test
    @DisplayName("포인트 적립 성공")
    void earnPoints_Success() {
        // given
        Long userId = 1L;
        Money earnAmount = Money.from(new BigDecimal("5000"));
        EarnPointCommand command = new EarnPointCommand(userId, earnAmount, "Test earn");

        // when
        PointTransactionResult result = sut.earnPoints(command);

        // then
        assertThat(result.type()).isEqualTo(PointTransactionType.EARN);
        assertThat(result.amount()).isEqualTo(earnAmount);
        assertThat(result.balanceAfter()).isEqualTo(earnAmount);
        assertThat(result.description()).isEqualTo("Test earn");
        assertThat(result.expireAt()).isNotNull();
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void usePoints_Success() {
        // given
        Long userId = 1L;
        Money earnAmount = Money.from(new BigDecimal("10000"));
        EarnPointCommand earnCommand = new EarnPointCommand(userId, earnAmount, "Initial earn");
        sut.earnPoints(earnCommand);

        Money useAmount = Money.from(new BigDecimal("3000"));
        UsePointCommand useCommand = new UsePointCommand(userId, useAmount, 15000, "Test use");

        // when
        PointTransactionResult result = sut.usePoints(useCommand);

        // then
        assertThat(result.type()).isEqualTo(PointTransactionType.USE);
        assertThat(result.amount()).isEqualTo(useAmount);
        assertThat(result.balanceAfter()).isEqualTo(Money.from(new BigDecimal("7000")));
        assertThat(result.description()).isEqualTo("Test use");
    }

    @Test
    @DisplayName("포인트 차감 시 잔액 부족 예외 발생")
    void usePoints_InsufficientBalance_ThrowsBusinessException() {
        // given
        Long userId = 1L;
        Money earnAmount = Money.from(new BigDecimal("5000"));
        EarnPointCommand earnCommand = new EarnPointCommand(userId, earnAmount, "Initial earn");
        sut.earnPoints(earnCommand);

        // 잔액(5,000)보다 많은 포인트(10,000) 사용 시도
        Money useAmount = Money.from(new BigDecimal("10000"));
        UsePointCommand useCommand = new UsePointCommand(userId, useAmount, 20000, "Test use");

        // when & then
        assertThatThrownBy(() -> sut.usePoints(useCommand))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("최소 사용 포인트 미만 시 예외 발생")
    void usePoints_BelowMinimumUsagePoint_ThrowsBusinessException() {
        // given
        Long userId = 1L;
        Money earnAmount = Money.from(new BigDecimal("10000"));
        EarnPointCommand earnCommand = new EarnPointCommand(userId, earnAmount, "Initial earn");
        sut.earnPoints(earnCommand);

        // 최소 사용 포인트(1,000)보다 적은 포인트(500) 사용 시도
        Money useAmount = Money.from(new BigDecimal("500"));
        UsePointCommand useCommand = new UsePointCommand(userId, useAmount, 15000, "Test use");

        // when & then
        assertThatThrownBy(() -> sut.usePoints(useCommand))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("최소 주문 금액 미만 시 예외 발생")
    void usePoints_BelowMinimumOrderAmount_ThrowsBusinessException() {
        // given
        Long userId = 1L;
        Money earnAmount = Money.from(new BigDecimal("10000"));
        EarnPointCommand earnCommand = new EarnPointCommand(userId, earnAmount, "Initial earn");
        sut.earnPoints(earnCommand);

        // 최소 주문 금액(10,000)보다 적은 주문(5,000)에서 포인트 사용 시도
        Money useAmount = Money.from(new BigDecimal("1000"));
        UsePointCommand useCommand = new UsePointCommand(userId, useAmount, 5000, "Test use");

        // when & then
        assertThatThrownBy(() -> sut.usePoints(useCommand))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("최대 잔액 초과 시 예외 발생")
    void earnPoints_ExceedMaxBalance_ThrowsBusinessException() {
        // given
        Long userId = 1L;

        // 최대 잔액에 가까운 금액 먼저 적립
        Money initialEarn = Money.from(new BigDecimal("900000"));
        EarnPointCommand initialCommand = new EarnPointCommand(userId, initialEarn, "Initial earn");
        sut.earnPoints(initialCommand);

        // 최대 잔액(1,000,000)을 초과하는 추가 적립 시도
        Money additionalEarn = Money.from(new BigDecimal("200000"));
        EarnPointCommand additionalCommand = new EarnPointCommand(userId, additionalEarn, "Additional earn");

        // when & then
        assertThatThrownBy(() -> sut.earnPoints(additionalCommand))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("포인트 잔액 조회 - 지갑이 없는 경우 새로 생성")
    void getWallet_WhenWalletNotExists_CreatesNew() {
        // given
        Long userId = 999L;

        // when
        PointWalletResult result = sut.getWallet(userId);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.balance()).isEqualTo(Money.ZERO);
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("포인트 거래 내역 조회")
    void getTransactions_Success() {
        // given
        Long userId = 1L;

        // 포인트 적립
        Money earnAmount = Money.from(new BigDecimal("10000"));
        sut.earnPoints(new EarnPointCommand(userId, earnAmount, "Earn 1"));

        // 포인트 사용
        Money useAmount = Money.from(new BigDecimal("3000"));
        sut.usePoints(new UsePointCommand(userId, useAmount, 15000, "Use 1"));

        // when
        var transactions = sut.getTransactions(userId);

        // then
        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).type()).isEqualTo(PointTransactionType.USE);
        assertThat(transactions.get(1).type()).isEqualTo(PointTransactionType.EARN);
    }
}