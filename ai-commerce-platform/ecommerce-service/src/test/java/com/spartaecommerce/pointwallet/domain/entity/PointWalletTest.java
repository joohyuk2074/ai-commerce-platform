package com.spartaecommerce.pointwallet.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PointWallet 도메인 엔티티")
class PointWalletTest {

    @Test
    @DisplayName("새 지갑 생성 시 초기값이 올바르게 설정된다")
    void createNew_InitializesWithCorrectDefaults() {
        // given
        Long userId = 1L;

        // when
        PointWallet wallet = PointWallet.createNew(userId);

        // then
        assertThat(wallet.getUserId()).isEqualTo(userId);
        assertThat(wallet.getBalance()).isEqualTo(Money.ZERO);
        assertThat(wallet.isActive()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideEarnPointsSuccessScenarios")
    @DisplayName("포인트 적립 성공 시나리오")
    void earnPoints_Success(
        String scenario,
        BigDecimal initialBalance,
        BigDecimal earnAmount,
        BigDecimal maxBalance,
        BigDecimal expectedBalance
    ) {
        // given
        PointWallet wallet = createWalletWithBalance(initialBalance);
        Money toEarn = Money.from(earnAmount);
        Money max = Money.from(maxBalance);

        // when
        wallet.earnPoints(toEarn, max);

        // then
        assertThat(wallet.getBalance().amount()).isEqualByComparingTo(expectedBalance);
        assertThat(wallet.isActive()).isTrue();
    }

    static Stream<Arguments> provideEarnPointsSuccessScenarios() {
        return Stream.of(
            Arguments.of("일반 적립", "0", "1000", "1000000", "1000"),
            Arguments.of("추가 적립", "5000", "3000", "1000000", "8000"),
            Arguments.of("최대 잔액 근처 적립", "999000", "1000", "1000000", "1000000"),
            Arguments.of("소액 적립", "100", "50", "1000000", "150"),
            Arguments.of("큰 금액 적립", "50000", "50000", "1000000", "100000")
        );
    }

    @ParameterizedTest
    @MethodSource("provideEarnPointsFailureScenarios")
    @DisplayName("포인트 적립 실패 시나리오")
    void earnPoints_Failure(
        String scenario,
        BigDecimal initialBalance,
        BigDecimal earnAmount,
        BigDecimal maxBalance,
        boolean isActive,
        ErrorCode expectedError
    ) {
        // given
        PointWallet wallet = createWalletWithBalance(initialBalance);
        if (!isActive) {
            wallet.deactivate();
        }
        Money toEarn = earnAmount != null ? Money.from(earnAmount) : null;
        Money max = Money.from(maxBalance);

        // when & then
        assertThatThrownBy(() -> wallet.earnPoints(toEarn, max))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(expectedError);
    }

    static Stream<Arguments> provideEarnPointsFailureScenarios() {
        return Stream.of(
            Arguments.of("최대 잔액 초과", "999000", "1001", "1000000", true, ErrorCode.INVALID_REQUEST),
            Arguments.of("비활성 지갑", "0", "1000", "1000000", false, ErrorCode.INVALID_REQUEST),
            Arguments.of("0원 적립", "1000", "0", "1000000", true, ErrorCode.INVALID_REQUEST)
        );
    }

    @Test
    @DisplayName("null 금액으로 적립 시 예외 발생")
    void earnPoints_NullAmount_ThrowsException() {
        // given
        PointWallet wallet = createWalletWithBalance(new BigDecimal("1000"));

        // when & then
        assertThatThrownBy(() -> wallet.earnPoints(null, Money.from(new BigDecimal("1000000"))))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @ParameterizedTest
    @MethodSource("provideUsePointsSuccessScenarios")
    @DisplayName("포인트 사용 성공 시나리오")
    void usePoints_Success(
        String scenario,
        BigDecimal initialBalance,
        BigDecimal useAmount,
        Integer minUsagePoint,
        BigDecimal expectedBalance
    ) {
        // given
        PointWallet wallet = createWalletWithBalance(initialBalance);
        Money toUse = Money.from(useAmount);

        // when
        wallet.usePoints(toUse, minUsagePoint);

        // then
        assertThat(wallet.getBalance().amount()).isEqualByComparingTo(expectedBalance);
        assertThat(wallet.isActive()).isTrue();
    }

    static Stream<Arguments> provideUsePointsSuccessScenarios() {
        return Stream.of(
            Arguments.of("일반 사용", "10000", "3000", 1000, "7000"),
            Arguments.of("최소 사용 포인트 정확히 사용", "5000", "1000", 1000, "4000"),
            Arguments.of("전액 사용", "5000", "5000", 1000, "0"),
            Arguments.of("큰 금액 사용", "100000", "50000", 1000, "50000"),
            Arguments.of("최소값보다 많이 사용", "20000", "15000", 1000, "5000")
        );
    }

    @ParameterizedTest
    @MethodSource("provideUsePointsFailureScenarios")
    @DisplayName("포인트 사용 실패 시나리오")
    void usePoints_Failure(
        String scenario,
        BigDecimal initialBalance,
        BigDecimal useAmount,
        Integer minUsagePoint,
        boolean isActive,
        ErrorCode expectedError
    ) {
        // given
        PointWallet wallet = createWalletWithBalance(initialBalance);
        if (!isActive) {
            wallet.deactivate();
        }
        Money toUse = useAmount != null ? Money.from(useAmount) : null;

        // when & then
        assertThatThrownBy(() -> wallet.usePoints(toUse, minUsagePoint))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(expectedError);
    }

    static Stream<Arguments> provideUsePointsFailureScenarios() {
        return Stream.of(
            Arguments.of("잔액 부족", "5000", "6000", 1000, true, ErrorCode.INVALID_REQUEST),
            Arguments.of("최소 사용 포인트 미만", "10000", "500", 1000, true, ErrorCode.INVALID_REQUEST),
            Arguments.of("비활성 지갑", "10000", "3000", 1000, false, ErrorCode.INVALID_REQUEST),
            Arguments.of("0원 사용", "10000", "0", 1000, true, ErrorCode.INVALID_REQUEST)
        );
    }

    @Test
    @DisplayName("null 금액으로 사용 시 예외 발생")
    void usePoints_NullAmount_ThrowsException() {
        // given
        PointWallet wallet = createWalletWithBalance(new BigDecimal("10000"));

        // when & then
        assertThatThrownBy(() -> wallet.usePoints(null, 1000))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @ParameterizedTest
    @MethodSource("provideExpirePointsScenarios")
    @DisplayName("포인트 만료 시나리오")
    void expirePoints_Scenarios(
        String scenario,
        BigDecimal initialBalance,
        BigDecimal expireAmount,
        BigDecimal expectedBalance
    ) {
        // given
        PointWallet wallet = createWalletWithBalance(initialBalance);
        Money toExpire = Money.from(expireAmount);

        // when
        wallet.expirePoints(toExpire);

        // then
        assertThat(wallet.getBalance().amount()).isEqualByComparingTo(expectedBalance);
    }

    static Stream<Arguments> provideExpirePointsScenarios() {
        return Stream.of(
            Arguments.of("일부 만료", "10000", "3000", "7000"),
            Arguments.of("전액 만료", "5000", "5000", "0"),
            Arguments.of("잔액보다 많이 만료 (전액 차감)", "3000", "5000", "0"),
            Arguments.of("소액 만료", "10000", "100", "9900")
        );
    }

    @Test
    @DisplayName("포인트 만료 시 0원이면 예외 발생")
    void expirePoints_ZeroAmount_ThrowsException() {
        // given
        PointWallet wallet = createWalletWithBalance(new BigDecimal("10000"));
        Money toExpire = Money.from(BigDecimal.ZERO);

        // when & then
        assertThatThrownBy(() -> wallet.expirePoints(toExpire))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("비활성 지갑에서 포인트 만료 시 예외 발생")
    void expirePoints_InactiveWallet_ThrowsException() {
        // given
        PointWallet wallet = createWalletWithBalance(new BigDecimal("10000"));
        wallet.deactivate();
        Money toExpire = Money.from(new BigDecimal("3000"));

        // when & then
        assertThatThrownBy(() -> wallet.expirePoints(toExpire))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("지갑 비활성화")
    void deactivate_SetsActiveToFalse() {
        // given
        PointWallet wallet = PointWallet.createNew(1L);
        assertThat(wallet.isActive()).isTrue();

        // when
        wallet.deactivate();

        // then
        assertThat(wallet.isActive()).isFalse();
    }

    @Test
    @DisplayName("비활성화된 지갑은 포인트 작업이 불가능하다")
    void deactivatedWallet_CannotPerformOperations() {
        // given
        PointWallet wallet = createWalletWithBalance(new BigDecimal("10000"));
        wallet.deactivate();

        // when & then
        assertThatThrownBy(() -> wallet.earnPoints(Money.from(new BigDecimal("1000")), Money.from(new BigDecimal("1000000"))))
            .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> wallet.usePoints(Money.from(new BigDecimal("1000")), 1000))
            .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> wallet.expirePoints(Money.from(new BigDecimal("1000"))))
            .isInstanceOf(BusinessException.class);
    }

    private PointWallet createWalletWithBalance(BigDecimal balance) {
        return PointWallet.builder()
            .walletId(1L)
            .userId(1L)
            .balance(Money.from(balance))
            .active(true)
            .build();
    }
}