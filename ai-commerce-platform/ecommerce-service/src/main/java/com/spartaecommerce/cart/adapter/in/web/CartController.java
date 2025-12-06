package com.spartaecommerce.cart.adapter.in.web;

import com.spartaecommerce.cart.application.dto.result.CartResult;
import com.spartaecommerce.cart.domain.command.CartClearCommand;
import com.spartaecommerce.cart.domain.command.CartRemoveItemCommand;
import com.spartaecommerce.cart.domain.port.in.*;
import com.spartaecommerce.cart.domain.query.CartGetQuery;
import com.spartaecommerce.cart.adapter.in.web.dto.request.CartAddItemRequest;
import com.spartaecommerce.cart.adapter.in.web.dto.request.CartUpdateItemQuantityRequest;
import com.spartaecommerce.cart.adapter.in.web.dto.response.CartResponse;
import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.web.annotation.AuthenticatedUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart-items")
@RequiredArgsConstructor
public class CartController {

    private final AddCartItemUseCase addCartItemUseCase;
    private final UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    private final GetCartUseCase getCartUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final ClearCartUseCase clearCartUseCase;

    @PostMapping
    public ResponseEntity<CommonResponse<Void>> addItem(
        @AuthenticatedUserId Long userId,
        @Valid @RequestBody CartAddItemRequest request
    ) {
        addCartItemUseCase.addItem(request.toCommand(userId));
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<CommonResponse<Void>> updateItemQuantity(
        @AuthenticatedUserId Long userId,
        @PathVariable Long productId,
        @Valid @RequestBody CartUpdateItemQuantityRequest request
    ) {
        updateCartItemQuantityUseCase.updateItemQuantity(request.toCommand(userId, productId));
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<CartResponse>> getCart(
        @AuthenticatedUserId Long userId
    ) {
        CartGetQuery query = new CartGetQuery(userId);
        CartResult result = getCartUseCase.get(query);
        CartResponse response = CartResponse.from(result);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<CommonResponse<Void>> removeItem(
        @AuthenticatedUserId Long userId,
        @PathVariable Long productId
    ) {
        CartRemoveItemCommand command = new CartRemoveItemCommand(userId, productId);
        removeCartItemUseCase.removeItem(command);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping
    public ResponseEntity<CommonResponse<Void>> clearCart(
        @AuthenticatedUserId Long userId
    ) {
        CartClearCommand command = new CartClearCommand(userId);
        clearCartUseCase.clear(command);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}