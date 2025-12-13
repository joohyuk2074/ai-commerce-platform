package com.spartaecommerce.refund.domain.port.out;

import com.spartaecommerce.refund.domain.entity.Refund;

public interface SaveRefundPort {

    Long save(Refund refund);
}
