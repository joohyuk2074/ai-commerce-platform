package com.spartaecommerce.pointwallet.domain.port.in;

import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.application.dto.command.EarnPointCommand;
import com.spartaecommerce.pointwallet.application.dto.command.UsePointCommand;

public interface PointWalletCommandUseCase {

    PointTransactionResult earnPoints(EarnPointCommand command);

    PointTransactionResult usePoints(UsePointCommand command);

    int expirePoints();
}
