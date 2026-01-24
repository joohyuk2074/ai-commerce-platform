package com.spartaecommerce.order.adapter.in.web;

import com.spartaecommerce.api.response.CommonResponse;
import com.spartaecommerce.api.response.PageResponse;
import com.spartaecommerce.order.adapter.in.web.dto.request.OrderSearchRequest;
import com.spartaecommerce.order.adapter.in.web.dto.response.OrderResponse;
import com.spartaecommerce.order.application.dto.query.OrderSearchQuery;
import com.spartaecommerce.order.application.dto.result.OrderResult;
import com.spartaecommerce.order.domain.port.in.OrderQueryUseCase;
import com.spartaecommerce.util.PageResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound Adapter for Order Query Operations (Read-only operations) Handles HTTP requests for
 * retrieving and searching orders
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderQueryController {

  private final OrderQueryUseCase orderQueryUseCase;

  @GetMapping("/orders")
  public ResponseEntity<CommonResponse<PageResponse<OrderResponse>>> search(
      OrderSearchRequest searchRequest
  ) {
    OrderSearchQuery searchQuery = searchRequest.toQuery();
    Page<OrderResult> orderInfoPage = orderQueryUseCase.search(searchQuery);
    Page<OrderResponse> orderResponse = orderInfoPage.map(OrderResponse::from);

    PageResponse<OrderResponse> pageResponse = PageResponseMapper.of(orderResponse);

    return ResponseEntity.ok(CommonResponse.success(pageResponse));
  }

  @GetMapping("/orders/{orderId}")
  public ResponseEntity<CommonResponse<OrderResponse>> getById(
      @PathVariable Long orderId
  ) {
    OrderResult orderResult = orderQueryUseCase.getById(orderId);
    OrderResponse orderResponse = OrderResponse.from(orderResult);

    return ResponseEntity.ok(CommonResponse.success(orderResponse));
  }
}
