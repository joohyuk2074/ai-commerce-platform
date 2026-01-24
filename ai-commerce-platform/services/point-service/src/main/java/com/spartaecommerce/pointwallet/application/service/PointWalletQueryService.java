package com.spartaecommerce.pointwallet.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.application.dto.result.PointWalletResult;
import com.spartaecommerce.common.domain.pointwallet.PointTransaction;
import com.spartaecommerce.common.domain.pointwallet.PointWallet;
import com.spartaecommerce.pointwallet.domain.port.in.PointWalletQueryUseCase;
import com.spartaecommerce.pointwallet.domain.port.out.LoadPointTransactionPort;
import com.spartaecommerce.pointwallet.domain.port.out.LoadPointWalletPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointWalletQueryService implements PointWalletQueryUseCase {

    private final LoadPointWalletPort loadPointWalletPort;
    private final LoadPointTransactionPort loadPointTransactionPort;

    /**
     * 포인트 잔액 조회
     */
    @Override
    public PointWalletResult getWallet(Long userId) {
        PointWallet wallet = loadPointWalletPort.findByUserId(userId)
            .orElseGet(() -> PointWallet.createNew(userId));

        return PointWalletResult.from(wallet);
    }

    /**
     * 포인트 거래 내역 조회
     */
    @Override
    public List<PointTransactionResult> getTransactions(Long userId) {
        PointWallet wallet = loadPointWalletPort.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                "Wallet not found for userId: " + userId
            ));

        List<PointTransaction> transactions = loadPointTransactionPort.findByWalletId(wallet.getWalletId());

        return transactions.stream()
            .map(PointTransactionResult::from)
            .toList();
    }
}
