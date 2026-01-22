package com.spartaecommerce.order.adapter.in.web;

import com.spartaecommerce.common.auth.Passport;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.common.web.annotation.CurrentPassport;
import com.spartaecommerce.order.adapter.in.web.dto.request.OrderCreateRequest;
import com.spartaecommerce.order.adapter.in.web.dto.request.OrderStatusUpdateRequest;
import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderStatusUpdateCommand;
import com.spartaecommerce.order.domain.port.in.OrderCommandUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "주문 관리", description = "주문 생성, 수정, 취소 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderCommandController {

    private final OrderCommandUseCase orderCommandUseCase;

    @Operation(
        summary = "주문 생성",
        description = "새로운 주문을 생성합니다. 상품 재고를 차감하고 포인트를 적립/사용 처리합니다.",
        security = @SecurityRequirement(name = "Passport Authentication")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "주문 생성 성공",
            content = @Content(schema = @Schema(implementation = IdResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검사 실패)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "재고 부족"
        )
    })
    @PostMapping("/orders")
    public ResponseEntity<CommonResponse<IdResponse>> create(
        @Parameter(hidden = true) @CurrentPassport Passport passport,
        @Valid @RequestBody OrderCreateRequest createRequest
    ) {
        OrderCreateCommand createCommand = createRequest.toCommand(passport);
        Long orderId = orderCommandUseCase.create(createCommand);

        return ResponseEntity
            .created(URI.create("/api/v1/orders/" + orderId))
            .body(CommonResponse.create(orderId));
    }

    @Operation(
        summary = "주문 상태 변경",
        description = "주문의 상태를 변경합니다. (예: PENDING -> CONFIRMED)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "주문 상태 변경 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음"
        )
    })
    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<Void> updateOrderStatus(
        @Parameter(description = "주문 ID", required = true, example = "1")
        @PathVariable Long orderId,
        @Valid @RequestBody OrderStatusUpdateRequest updateRequest
    ) {
        OrderStatusUpdateCommand updateCommand = updateRequest.toCommand(orderId);
        orderCommandUseCase.updateOrderStatus(updateCommand);

        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "주문 취소",
        description = "주문을 취소하고 재고와 포인트를 복구합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "주문 취소 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "취소할 수 없는 상태"
        )
    })
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
        @Parameter(description = "주문 ID", required = true, example = "1")
        @PathVariable Long orderId
    ) {
        orderCommandUseCase.cancel(orderId);
        return ResponseEntity.noContent().build();
    }
}
