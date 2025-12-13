package com.spartaecommerce.refund.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.refund.adapter.in.web.dto.request.RefundCreateRequest;
import com.spartaecommerce.refund.adapter.in.web.dto.request.RefundProcessRequest;
import com.spartaecommerce.refund.application.dto.command.RefundCreateCommand;
import com.spartaecommerce.refund.application.dto.command.RefundProcessCommand;
import com.spartaecommerce.refund.domain.port.in.RefundCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RefundCommandController {

    private final RefundCommandUseCase refundCommandUseCase;

    @PostMapping("/refunds")
    public ResponseEntity<CommonResponse<IdResponse>> createRefund(
        @Valid @RequestBody RefundCreateRequest request
    ) {
        RefundCreateCommand createCommand = request.toCommand();
        Long refundId = refundCommandUseCase.create(createCommand);

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
        refundCommandUseCase.process(processCommand);
        return ResponseEntity.noContent()
            .build();
    }
}
