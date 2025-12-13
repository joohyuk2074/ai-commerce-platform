package com.spartaecommerce.coupon.domain.port.in;

import com.spartaecommerce.coupon.application.dto.command.CreateCouponCommand;

public interface CreateCouponUseCase {

    Long create(CreateCouponCommand command);
}
