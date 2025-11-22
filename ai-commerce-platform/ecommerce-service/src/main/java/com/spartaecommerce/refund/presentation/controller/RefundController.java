package com.spartaecommerce.refund.presentation.controller;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.refund.application.RefundService;
import com.spartaecommerce.refund.application.dto.result.RefundResult;
import com.spartaecommerce.refund.application.dto.command.RefundCreateCommand;
import com.spartaecommerce.refund.application.dto.command.RefundProcessCommand;
import com.spartaecommerce.refund.application.dto.query.RefundSearchQuery;
import com.spartaecommerce.refund.presentation.controller.dto.request.RefundCreateRequest;
import com.spartaecommerce.refund.presentation.controller.dto.request.RefundProcessRequest;
import com.spartaecommerce.refund.presentation.controller.dto.request.RefundSearchRequest;
import com.spartaecommerce.refund.presentation.controller.dto.response.RefundResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/refunds")
    public ResponseEntity<CommonResponse<IdResponse>> createRefund(
        @Valid @RequestBody RefundCreateRequest request
    ) {
        RefundCreateCommand createCommand = request.toCommand();
        Long refundId = refundService.create(createCommand);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CommonResponse.create(refundId));
    }

    @PatchMapping("/refunds/{refundId}")
    public ResponseEntity<Void> processRefund(
        @PathVariable Long refundId,
        @Valid @RequestBody RefundProcessRequest request
    ) {
        RefundProcessCommand processCommand = request.toCommand(refundId);
        refundService.process(processCommand);
        return ResponseEntity.noContent()
            .build();
    }

    @GetMapping("/refunds")
    public ResponseEntity<CommonResponse<PageResponse<RefundResponse>>> searchRefunds(
        @Valid RefundSearchRequest searchRequest
    ) {
        RefundSearchQuery searchQuery = searchRequest.toQuery();
        Page<RefundResult> refundPage = refundService.search(searchQuery);
        Page<RefundResponse> refundResponse = refundPage.map(RefundResponse::from);

        PageResponse<RefundResponse> refundResponsePageResponse = PageResponse.of(refundResponse);

        return ResponseEntity.ok(CommonResponse.success(refundResponsePageResponse));
    }
}