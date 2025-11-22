package com.spartaecommerce.pointwallet.application;

import com.spartaecommerce.common.config.properties.PointsProperties;
import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.common.util.DateTimeHolder;
import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.application.dto.result.PointWalletResult;
import com.spartaecommerce.pointwallet.domain.command.EarnPointCommand;
import com.spartaecommerce.pointwallet.domain.command.UsePointCommand;
import com.spartaecommerce.pointwallet.domain.entity.PointTransaction;
import com.spartaecommerce.pointwallet.domain.entity.PointTransactionType;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.repository.PointTransactionRepository;
import com.spartaecommerce.pointwallet.domain.repository.PointWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointWalletService {

    private final PointWalletRepository pointWalletRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointsProperties pointsProperties;
    private final DateTimeHolder dateTimeHolder;

    @Transactional
    public PointTransactionResult earnPoints(EarnPointCommand command) {
        PointWallet wallet = pointWalletRepository.findByUserId(command.userId())
            .orElseGet(() -> {
                PointWallet newWallet = PointWallet.createNew(command.userId());
                pointWalletRepository.save(newWallet);
                return pointWalletRepository.getByUserId(command.userId());
            });

        // 최대 잔액 제한
        Money maxBalance = Money.from(pointsProperties.getMaxBalance());

        // 포인트 적립
        wallet.earnPoints(command.amount(), maxBalance);

        // 만료일 계산 (현재 시간 + expireDays)
        LocalDateTime expireAt = dateTimeHolder.getCurrentDateTime().plusDays(pointsProperties.getExpireDays());

        // 거래 기록 생성
        PointTransaction transaction = PointTransaction.createEarn(
            wallet.getWalletId(),
            command.amount(),
            wallet.getBalance(),
            expireAt,
            command.description()
        );

        // 저장
        pointWalletRepository.save(wallet);
        Long transactionId = pointTransactionRepository.save(transaction);

        // 이벤트 발행 (로그)
        publishPointEvent(PointTransactionType.EARN, command.userId(), command.amount(), command.description());

        // 저장된 거래 조회
        PointTransaction savedTransaction = PointTransaction.builder()
            .transactionId(transactionId)
            .walletId(wallet.getWalletId())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .balanceAfter(transaction.getBalanceAfter())
            .expireAt(transaction.getExpireAt())
            .description(transaction.getDescription())
            .createdAt(LocalDateTime.now())
            .build();

        return PointTransactionResult.from(savedTransaction);
    }

    @Transactional
    public PointTransactionResult usePoints(UsePointCommand command) {
        // 최소 주문 금액 검증
        if (command.orderAmount() < pointsProperties.getMinUsageAmount()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Minimum order amount is " + pointsProperties.getMinUsageAmount() +
                ". Order amount: " + command.orderAmount()
            );
        }

        // 지갑 조회
        PointWallet wallet = pointWalletRepository.getByUserId(command.userId());

        // 포인트 사용 (최소 사용 포인트 검증 포함)
        wallet.usePoints(command.amount(), pointsProperties.getMinUsagePoint());

        // 거래 기록 생성
        PointTransaction transaction = PointTransaction.createUse(
            wallet.getWalletId(),
            command.amount(),
            wallet.getBalance(),
            command.description()
        );

        // 저장
        pointWalletRepository.save(wallet);
        Long transactionId = pointTransactionRepository.save(transaction);

        // 이벤트 발행 (로그)
        publishPointEvent(PointTransactionType.USE, command.userId(), command.amount(), command.description());

        // 저장된 거래 조회
        PointTransaction savedTransaction = PointTransaction.builder()
            .transactionId(transactionId)
            .walletId(wallet.getWalletId())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .balanceAfter(transaction.getBalanceAfter())
            .description(transaction.getDescription())
            .createdAt(LocalDateTime.now())
            .build();

        return PointTransactionResult.from(savedTransaction);
    }

    /**
     * 포인트 잔액 조회
     */
    public PointWalletResult getWallet(Long userId) {
        PointWallet wallet = pointWalletRepository.findByUserId(userId)
            .orElseGet(() -> PointWallet.createNew(userId));

        return PointWalletResult.from(wallet);
    }

    /**
     * 포인트 거래 내역 조회
     */
    public List<PointTransactionResult> getTransactions(Long userId) {
        PointWallet wallet = pointWalletRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Wallet not found for userId: " + userId
            ));

        List<PointTransaction> transactions = pointTransactionRepository.findByWalletId(wallet.getWalletId());

        return transactions.stream()
            .map(PointTransactionResult::from)
            .toList();
    }

    /**
     * 만료된 포인트 처리 (수동 API)
     */
    @Transactional
    public int expirePoints() {
        LocalDateTime now = LocalDateTime.now();
        List<PointTransaction> expiredTransactions = pointTransactionRepository.findExpiredTransactions(now);

        int expiredCount = 0;

        for (PointTransaction earnTransaction : expiredTransactions) {
            // 지갑 조회
            PointWallet wallet = pointWalletRepository.findByUserId(earnTransaction.getWalletId())
                .orElse(null);

            if (wallet == null || !wallet.isActive()) {
                continue;
            }

            // 포인트 만료 처리
            wallet.expirePoints(earnTransaction.getAmount());

            // 만료 거래 기록 생성
            PointTransaction expireTransaction = PointTransaction.createExpire(
                wallet.getWalletId(),
                earnTransaction.getAmount(),
                wallet.getBalance(),
                "Expired points from transaction: " + earnTransaction.getTransactionId()
            );

            // 저장
            pointWalletRepository.save(wallet);
            pointTransactionRepository.save(expireTransaction);

            // 이벤트 발행 (로그)
            publishPointEvent(
                PointTransactionType.EXPIRE,
                wallet.getUserId(),
                earnTransaction.getAmount(),
                "Point expired"
            );

            expiredCount++;
        }

        log.info("Expired {} point transactions", expiredCount);

        return expiredCount;
    }

    /**
     * 포인트 이벤트 발행 (로그 기반)
     */
    private void publishPointEvent(PointTransactionType type, Long userId, Money amount, String description) {
        log.info("[POINT EVENT] type={}, userId={}, amount={}, description={}",
            type, userId, amount.amount(), description);
    }
}