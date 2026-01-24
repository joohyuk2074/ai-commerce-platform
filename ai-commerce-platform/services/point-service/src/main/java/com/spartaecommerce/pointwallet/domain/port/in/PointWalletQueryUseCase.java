package com.spartaecommerce.pointwallet.domain.port.in;

import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.application.dto.result.PointWalletResult;

import java.util.List;

public interface PointWalletQueryUseCase {

    PointWalletResult getWallet(Long userId);

    List<PointTransactionResult> getTransactions(Long userId);
}
