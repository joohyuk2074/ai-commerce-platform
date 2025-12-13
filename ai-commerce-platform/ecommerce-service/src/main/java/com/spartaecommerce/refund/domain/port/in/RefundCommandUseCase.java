package com.spartaecommerce.refund.domain.port.in;

import com.spartaecommerce.refund.application.dto.command.RefundCreateCommand;
import com.spartaecommerce.refund.application.dto.command.RefundProcessCommand;

public interface RefundCommandUseCase {

    Long create(RefundCreateCommand createCommand);

    void process(RefundProcessCommand processCommand);
}
