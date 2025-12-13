package com.spartaecommerce.coupon.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.common.domain.IdResponse;
import com.spartaecommerce.coupon.adapter.in.web.dto.request.CreateCouponRequest;
import com.spartaecommerce.coupon.domain.port.in.CreateCouponUseCase;
import com.spartaecommerce.coupon.domain.port.in.DeleteCouponUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CouponCommandController {

    private final CreateCouponUseCase createCouponUseCase;
    private final DeleteCouponUseCase deleteCouponUseCase;

    @PostMapping("/coupons")
    public ResponseEntity<CommonResponse<IdResponse>> createCoupon(
        @Valid @RequestBody CreateCouponRequest request
    ) {
        Long couponId = createCouponUseCase.create(request.toCommand());

        return ResponseEntity
            .created(URI.create("/api/v1/coupons/" + couponId))
            .body(CommonResponse.create(couponId));
    }

    @DeleteMapping("/coupons/{couponId}")
    public ResponseEntity<CommonResponse<Void>> deleteCoupon(
        @PathVariable Long couponId
    ) {
        deleteCouponUseCase.delete(couponId);

        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
