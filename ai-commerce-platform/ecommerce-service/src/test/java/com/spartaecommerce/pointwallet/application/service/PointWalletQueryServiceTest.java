package com.spartaecommerce.pointwallet.application.service;

import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.util.DateTimeHolder;
import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.application.dto.result.PointWalletResult;
import com.spartaecommerce.pointwallet.application.dto.command.EarnPointCommand;
import com.spartaecommerce.pointwallet.application.dto.command.UsePointCommand;
import com.spartaecommerce.pointwallet.domain.entity.PointTransactionType;
import com.spartaecommerce.pointwallet.domain.port.out.PointTransactionFakeRepository;
import com.spartaecommerce.pointwallet.domain.port.out.PointWalletFakeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PointWalletQueryService")
class PointWalletQueryServiceTest {

    private PointWalletQueryService sut;
    private PointWalletCommandService commandService;
    private PointWalletFakeRepository walletRepository;
    private PointTransactionFakeRepository transactionRepository;

    @BeforeEach
    void setUp() {
        walletRepository = new PointWalletFakeRepository();
        transactionRepository = new PointTransactionFakeRepository();

        sut = new PointWalletQueryService(
            walletRepository,
            transactionRepository
        );

        // CommandService는 테스트 데이터 준비용
        PointsProperties pointsProperties = createPointsProperties();
        TestDateTimeHolder dateTimeHolder = new TestDateTimeHolder();
        commandService = new PointWalletCommandService(
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

    @Test
    @DisplayName("사용자의 포인트 지갑을 조회한다")
    void getWallet_ExistingUser_ReturnsWallet() {
        // given
        Long userId = 1L;
        Money amount = Money.from(new BigDecimal("5000"));
        commandService.earnPoints(new EarnPointCommand(userId, amount, "적립"));

        // when
        PointWalletResult result = sut.getWallet(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.balance()).isEqualTo(amount);
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("포인트 지갑이 없는 사용자는 잔액 0인 새 지갑을 반환한다")
    void getWallet_NewUser_ReturnsNewWallet() {
        // given
        Long userId = 999L;

        // when
        PointWalletResult result = sut.getWallet(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.balance()).isEqualTo(Money.ZERO);
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("사용자의 포인트 거래 내역을 조회한다")
    void getTransactions_ExistingUser_ReturnsTransactions() {
        // given
        Long userId = 1L;
        Money earnAmount = Money.from(new BigDecimal("5000"));
        Money useAmount = Money.from(new BigDecimal("3000"));

        commandService.earnPoints(new EarnPointCommand(userId, earnAmount, "적립"));
        commandService.usePoints(new UsePointCommand(userId, useAmount, 10000, "사용"));

        // when
        List<PointTransactionResult> results = sut.getTransactions(userId);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).type()).isEqualTo(PointTransactionType.USE); // 최신순
        assertThat(results.get(0).amount()).isEqualTo(useAmount);
        assertThat(results.get(1).type()).isEqualTo(PointTransactionType.EARN);
        assertThat(results.get(1).amount()).isEqualTo(earnAmount);
    }

    @Test
    @DisplayName("포인트 지갑이 없는 사용자의 거래 내역 조회 시 예외가 발생한다")
    void getTransactions_NonExistingUser_ThrowsException() {
        // given
        Long userId = 999L;

        // when & then
        assertThatThrownBy(() -> sut.getTransactions(userId))
            .isInstanceOf(BusinessException.class);
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
    }
}
