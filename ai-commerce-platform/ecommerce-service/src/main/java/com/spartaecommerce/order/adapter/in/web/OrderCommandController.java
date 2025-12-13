package com.spartaecommerce.order.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.order.adapter.in.web.dto.request.OrderCreateRequest;
import com.spartaecommerce.order.adapter.in.web.dto.request.OrderStatusUpdateRequest;
import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderStatusUpdateCommand;
import com.spartaecommerce.order.domain.port.in.OrderCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Inbound Adapter for Order Command Operations (State-changing operations)
 * Handles HTTP requests for creating, updating, and canceling orders
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderCommandController {

    private final OrderCommandUseCase orderCommandUseCase;

    @PostMapping("/orders")
    public ResponseEntity<CommonResponse<IdResponse>> create(
        @Valid @RequestBody OrderCreateRequest createRequest
    ) {
        OrderCreateCommand createCommand = createRequest.toCommand();
        Long orderId = orderCommandUseCase.create(createCommand);

        return ResponseEntity
            .created(URI.create("/api/v1/orders/" + orderId))
            .body(CommonResponse.create(orderId));
    }

    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<Void> updateOrderStatus(
        @PathVariable Long orderId,
        @Valid @RequestBody OrderStatusUpdateRequest updateRequest
    ) {
        OrderStatusUpdateCommand updateCommand = updateRequest.toCommand(orderId);
        orderCommandUseCase.updateOrderStatus(updateCommand);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderCommandUseCase.cancel(orderId);
        return ResponseEntity.noContent().build();
    }
}
