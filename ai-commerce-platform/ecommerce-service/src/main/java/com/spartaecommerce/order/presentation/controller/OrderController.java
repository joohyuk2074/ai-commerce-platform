package com.spartaecommerce.order.presentation.controller;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.order.application.OrderCommandService;
import com.spartaecommerce.order.application.OrderQueryService;
import com.spartaecommerce.order.application.dto.result.OrderResult;
import com.spartaecommerce.order.application.dto.command.OrderCreateCommand;
import com.spartaecommerce.order.application.dto.command.OrderStatusUpdateCommand;
import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.presentation.controller.dto.request.OrderCreateRequest;
import com.spartaecommerce.order.presentation.controller.dto.request.OrderSearchRequest;
import com.spartaecommerce.order.presentation.controller.dto.request.OrderStatusUpdateRequest;
import com.spartaecommerce.order.presentation.controller.dto.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    @PostMapping("/orders")
    public ResponseEntity<CommonResponse<IdResponse>> create(
        @Valid @RequestBody OrderCreateRequest createRequest
    ) {
        OrderCreateCommand createCommand = createRequest.toCommand();

        Long orderId = orderCommandService.create(createCommand);

        return ResponseEntity
            .created(URI.create("/api/v1/orders/" + orderId))
            .body(CommonResponse.create(orderId));
    }

    @GetMapping("/orders")
    public ResponseEntity<CommonResponse<PageResponse<OrderResponse>>> search(
        OrderSearchRequest searchRequest
    ) {
        OrderSearchQuery searchQuery = searchRequest.toQuery();
        Page<OrderResult> orderInfoPage = orderQueryService.search(searchQuery);
        Page<OrderResponse> orderResponse = orderInfoPage.map(OrderResponse::from);

        PageResponse<OrderResponse> pageResponse = PageResponse.of(orderResponse);

        return ResponseEntity.ok(CommonResponse.success(pageResponse));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<CommonResponse<OrderResponse>> getById(
        @PathVariable Long orderId
    ) {
        OrderResult orderResult = orderQueryService.getById(orderId);
        OrderResponse orderResponse = OrderResponse.from(orderResult);

        return ResponseEntity.ok(CommonResponse.success(orderResponse));
    }

    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<Void> updateOrderStatus(
        @PathVariable Long orderId,
        @Valid @RequestBody OrderStatusUpdateRequest updateRequest
    ) {
        OrderStatusUpdateCommand updateCommand = updateRequest.toCommand(orderId);

        orderCommandService.updateOrderStatus(updateCommand);

        return ResponseEntity.noContent()
            .build();
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderCommandService.cancel(orderId);
        return ResponseEntity.noContent()
            .build();
    }
}
