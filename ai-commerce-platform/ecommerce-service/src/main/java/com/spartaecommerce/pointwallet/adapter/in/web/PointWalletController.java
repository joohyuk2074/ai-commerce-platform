package com.spartaecommerce.pointwallet.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.pointwallet.adapter.in.web.dto.request.EarnPointRequest;
import com.spartaecommerce.pointwallet.adapter.in.web.dto.request.UsePointRequest;
import com.spartaecommerce.pointwallet.adapter.in.web.dto.response.PointTransactionResponse;
import com.spartaecommerce.pointwallet.adapter.in.web.dto.response.PointWalletResponse;
import com.spartaecommerce.pointwallet.application.dto.result.PointTransactionResult;
import com.spartaecommerce.pointwallet.application.dto.result.PointWalletResult;
import com.spartaecommerce.pointwallet.application.dto.command.EarnPointCommand;
import com.spartaecommerce.pointwallet.application.dto.command.UsePointCommand;
import com.spartaecommerce.pointwallet.domain.port.in.PointWalletCommandUseCase;
import com.spartaecommerce.pointwallet.domain.port.in.PointWalletQueryUseCase;
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

    private final PointWalletCommandUseCase pointWalletCommandUseCase;
    private final PointWalletQueryUseCase pointWalletQueryUseCase;

    @PostMapping("/earn")
    public ResponseEntity<CommonResponse<PointTransactionResponse>> earnPoints(
        @Valid @RequestBody EarnPointRequest request
    ) {
        EarnPointCommand command = request.toCommand();
        PointTransactionResult result = pointWalletCommandUseCase.earnPoints(command);
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
        PointTransactionResult result = pointWalletCommandUseCase.usePoints(command);
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
        PointWalletResult result = pointWalletQueryUseCase.getWallet(userId);
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
        List<PointTransactionResult> results = pointWalletQueryUseCase.getTransactions(userId);
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
        int expiredCount = pointWalletCommandUseCase.expirePoints();

        return ResponseEntity.ok(
            CommonResponse.success(Map.of("expiredCount", expiredCount))
        );
    }
}
