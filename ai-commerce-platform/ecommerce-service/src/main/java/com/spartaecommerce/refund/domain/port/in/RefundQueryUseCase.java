package com.spartaecommerce.refund.domain.port.in;

import com.spartaecommerce.refund.application.dto.query.RefundSearchQuery;
import com.spartaecommerce.refund.application.dto.result.RefundResult;
import org.springframework.data.domain.Page;

public interface RefundQueryUseCase {

    Page<RefundResult> search(RefundSearchQuery searchQuery);
}
