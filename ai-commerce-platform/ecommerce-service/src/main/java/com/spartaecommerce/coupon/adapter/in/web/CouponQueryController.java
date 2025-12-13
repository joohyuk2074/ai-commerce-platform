package com.spartaecommerce.coupon.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.CustomPageable;
import com.spartaecommerce.common.domain.PageResponse;
import com.spartaecommerce.coupon.adapter.in.web.dto.response.CouponResponse;
import com.spartaecommerce.coupon.adapter.in.web.dto.response.MaxDiscountResponse;
import com.spartaecommerce.coupon.application.dto.result.CouponResult;
import com.spartaecommerce.coupon.application.dto.result.MaxDiscountResult;
import com.spartaecommerce.coupon.domain.port.in.CouponQueryUseCase;
import com.spartaecommerce.coupon.domain.port.in.GetMaxDiscountForProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CouponQueryController {

    private final CouponQueryUseCase couponQueryUseCase;
    private final GetMaxDiscountForProductUseCase getMaxDiscountForProductUseCase;

    @GetMapping("/coupons/{couponId}")
    public ResponseEntity<CommonResponse<CouponResponse>> getCoupon(
        @PathVariable Long couponId
    ) {
        CouponResult result = couponQueryUseCase.getById(couponId);
        CouponResponse response = CouponResponse.from(result);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/coupons")
    public ResponseEntity<CommonResponse<PageResponse<CouponResponse>>> searchCoupons(
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction
    ) {
        CustomPageable pageable = CustomPageable.of(page, size, sortBy, direction);
        Page<CouponResult> resultPage = couponQueryUseCase.search(isActive, pageable);
        Page<CouponResponse> responsePage = resultPage.map(CouponResponse::from);

        PageResponse<CouponResponse> pageResponse = PageResponse.of(responsePage);

        return ResponseEntity.ok(CommonResponse.success(pageResponse));
    }

    @GetMapping("/products/{productId}/max-discount")
    public ResponseEntity<CommonResponse<MaxDiscountResponse>> getMaxDiscountForProduct(
        @PathVariable Long productId
    ) {
        MaxDiscountResult result = getMaxDiscountForProductUseCase.getMaxDiscount(productId);
        MaxDiscountResponse response = MaxDiscountResponse.from(result);

        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
