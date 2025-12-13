package com.spartaecommerce.coupon.adapter.in.web;

import com.spartaecommerce.common.domain.CommonResponse;
import com.spartaecommerce.coupon.adapter.in.web.dto.request.IssueCouponsRequest;
import com.spartaecommerce.coupon.adapter.in.web.dto.request.RegisterCouponRequest;
import com.spartaecommerce.coupon.adapter.in.web.dto.response.CouponUserResponse;
import com.spartaecommerce.coupon.adapter.in.web.dto.response.IssueCouponsResponse;
import com.spartaecommerce.coupon.application.dto.result.CouponUserResult;
import com.spartaecommerce.coupon.domain.port.in.GetCouponUserUseCase;
import com.spartaecommerce.coupon.domain.port.in.IssueCouponsUseCase;
import com.spartaecommerce.coupon.domain.port.in.RegisterCouponUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 쿠폰 컨트롤러 (Inbound Adapter)
 * UseCases(Inbound Ports)에 의존합니다
 */
@RestController
@RequestMapping("/api/v1/coupon-users")
@RequiredArgsConstructor
public class CouponUserController {

    private final IssueCouponsUseCase issueCouponsUseCase;
    private final RegisterCouponUseCase registerCouponUseCase;
    private final GetCouponUserUseCase getCouponUserUseCase;

    /**
     * 쿠폰 대량 발급 API
     * 요청된 수량만큼 중복되지 않는 쿠폰 코드를 생성하고 PENDING 상태로 저장합니다.
     *
     * @param request 쿠폰 발급 요청 (couponId, quantity)
     * @return 발급된 쿠폰 정보 (issuedCount, couponUserIds)
     */
    @PostMapping("/issue")
    public ResponseEntity<CommonResponse<IssueCouponsResponse>> issueCoupons(
        @Valid @RequestBody IssueCouponsRequest request
    ) {
        List<Long> couponUserIds = issueCouponsUseCase.issue(request.toCommand());
        IssueCouponsResponse response = IssueCouponsResponse.of(couponUserIds);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 오프라인 쿠폰 등록 API
     * 쿠폰 코드를 검증하고 사용자에게 쿠폰을 발급합니다.
     *
     * @param request 쿠폰 등록 요청 (couponCode, userId)
     * @return 등록된 쿠폰 정보
     */
    @PostMapping("/register")
    public ResponseEntity<CommonResponse<CouponUserResponse>> registerCoupon(
        @Valid @RequestBody RegisterCouponRequest request
    ) {
        CouponUserResult result = registerCouponUseCase.register(request.toCommand());
        CouponUserResponse response = CouponUserResponse.from(result);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 쿠폰 코드로 쿠폰 조회 API
     *
     * @param code 쿠폰 코드
     * @return 쿠폰 정보
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<CommonResponse<CouponUserResponse>> getCouponByCode(
        @PathVariable String code
    ) {
        CouponUserResult result = getCouponUserUseCase.getByCode(code);
        CouponUserResponse response = CouponUserResponse.from(result);

        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
