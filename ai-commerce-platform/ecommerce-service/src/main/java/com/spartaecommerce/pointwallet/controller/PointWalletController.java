package com.spartaecommerce.pointwallet.controller;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.pointwallet.application.PointWalletService;
import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.application.dto.result.PointWalletResult;
import com.spartaecommerce.pointwallet.domain.command.EarnPointCommand;
import com.spartaecommerce.pointwallet.domain.command.UsePointCommand;
import com.spartaecommerce.pointwallet.controller.dto.request.EarnPointRequest;
import com.spartaecommerce.pointwallet.controller.dto.request.UsePointRequest;
import com.spartaecommerce.pointwallet.controller.dto.response.PointTransactionResponse;
import com.spartaecommerce.pointwallet.controller.dto.response.PointWalletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointWalletController {

    private final PointWalletService pointWalletService;

    @PostMapping("/earn")
    public ResponseEntity<CommonResponse<PointTransactionResponse>> earnPoints(
        @Valid @RequestBody EarnPointRequest request
    ) {
        EarnPointCommand command = request.toCommand();
        PointTransactionResult result = pointWalletService.earnPoints(command);
        PointTransactionResponse response = PointTransactionResponse.from(result);

        return ResponseEntity
            .created(URI.create("/api/v1/points/transactions/" + result.transactionId()))
            .body(CommonResponse.success(response));
    }

    /**
     * 포인트 사용
     */
    @PostMapping("/use")
    public ResponseEntity<CommonResponse<PointTransactionResponse>> usePoints(
        @Valid @RequestBody UsePointRequest request
    ) {
        UsePointCommand command = request.toCommand();
        PointTransactionResult result = pointWalletService.usePoints(command);
        PointTransactionResponse response = PointTransactionResponse.from(result);

        return ResponseEntity
            .created(URI.create("/api/v1/points/transactions/" + result.transactionId()))
            .body(CommonResponse.success(response));
    }

    /**
     * 포인트 잔액 조회
     */
    @GetMapping("/wallet/{userId}")
    public ResponseEntity<CommonResponse<PointWalletResponse>> getWallet(
        @PathVariable Long userId
    ) {
        PointWalletResult result = pointWalletService.getWallet(userId);
        PointWalletResponse response = PointWalletResponse.from(result);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 포인트 거래 내역 조회
     */
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<CommonResponse<List<PointTransactionResponse>>> getTransactions(
        @PathVariable Long userId
    ) {
        List<PointTransactionResult> results = pointWalletService.getTransactions(userId);
        List<PointTransactionResponse> responses = results.stream()
            .map(PointTransactionResponse::from)
            .toList();

        return ResponseEntity.ok(CommonResponse.success(responses));
    }

    /**
     * 만료된 포인트 처리 (수동 API)
     */
    @PostMapping("/expire")
    public ResponseEntity<CommonResponse<Map<String, Integer>>> expirePoints() {
        int expiredCount = pointWalletService.expirePoints();

        return ResponseEntity.ok(
            CommonResponse.success(Map.of("expiredCount", expiredCount))
        );
    }
}