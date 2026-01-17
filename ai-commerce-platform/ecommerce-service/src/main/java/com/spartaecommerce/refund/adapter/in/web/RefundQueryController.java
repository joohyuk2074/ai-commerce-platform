package com.spartaecommerce.refund.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.refund.adapter.in.web.dto.request.RefundSearchRequest;
import com.spartaecommerce.refund.adapter.in.web.dto.response.RefundResponse;
import com.spartaecommerce.refund.application.dto.query.RefundSearchQuery;
import com.spartaecommerce.refund.application.dto.result.RefundResult;
import com.spartaecommerce.refund.domain.port.in.RefundQueryUseCase;
import com.spartaecommerce.util.PageResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RefundQueryController {

    private final RefundQueryUseCase refundQueryUseCase;

    @GetMapping("/refunds")
    public ResponseEntity<CommonResponse<PageResponse<RefundResponse>>> searchRefunds(
        @Valid RefundSearchRequest searchRequest
    ) {
        RefundSearchQuery searchQuery = searchRequest.toQuery();
        Page<RefundResult> refundPage = refundQueryUseCase.search(searchQuery);
        Page<RefundResponse> refundResponse = refundPage.map(RefundResponse::from);

        PageResponse<RefundResponse> refundResponsePageResponse = PageResponseMapper.of(refundResponse);

        return ResponseEntity.ok(CommonResponse.success(refundResponsePageResponse));
    }
}
