package com.spartaecommerce.cart.presentation.controller;

import com.spartaecommerce.cart.application.CartService;
import com.spartaecommerce.cart.application.dto.result.CartResult;
import com.spartaecommerce.cart.domain.command.CartClearCommand;
import com.spartaecommerce.cart.domain.command.CartRemoveItemCommand;
import com.spartaecommerce.cart.domain.query.CartGetQuery;
import com.spartaecommerce.cart.presentation.controller.dto.request.CartAddItemRequest;
import com.spartaecommerce.cart.presentation.controller.dto.request.CartUpdateItemQuantityRequest;
import com.spartaecommerce.cart.presentation.controller.dto.response.CartResponse;
import com.spartaecommerce.common.domain.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/carts/{userId}/items")
    public ResponseEntity<CommonResponse<Void>> addItem(
        @PathVariable Long userId,
        @Valid @RequestBody CartAddItemRequest request
    ) {
        cartService.addItem(request.toCommand(userId));
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @PatchMapping("/carts/{userId}/items/{productId}")
    public ResponseEntity<CommonResponse<Void>> updateItemQuantity(
        @PathVariable Long userId,
        @PathVariable Long productId,
        @Valid @RequestBody CartUpdateItemQuantityRequest request
    ) {
        cartService.updateItemQuantity(request.toCommand(userId, productId));
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @GetMapping("/carts/{userId}")
    public ResponseEntity<CommonResponse<CartResponse>> getCart(
        @PathVariable Long userId
    ) {
        CartGetQuery query = new CartGetQuery(userId);
        CartResult result = cartService.get(query);
        CartResponse response = CartResponse.from(result);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @DeleteMapping("/carts/{userId}/items/{productId}")
    public ResponseEntity<CommonResponse<Void>> removeItem(
        @PathVariable Long userId,
        @PathVariable Long productId
    ) {
        CartRemoveItemCommand command = new CartRemoveItemCommand(userId, productId);
        cartService.removeItem(command);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/carts/{userId}")
    public ResponseEntity<CommonResponse<Void>> clearCart(
        @PathVariable Long userId
    ) {
        CartClearCommand command = new CartClearCommand(userId);
        cartService.clear(command);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}