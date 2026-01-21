package com.spartaecommerce.coupon.application.service;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.common.util.DateTimeHolder;
import com.spartaecommerce.coupon.application.dto.result.MaxDiscountResult;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.port.in.GetMaxDiscountForProductUseCase;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponPort;
import com.spartaecommerce.coupon.domain.port.out.LoadProductPort;
import com.spartaecommerce.coupon.domain.service.CouponDiscountCalculator;
import com.spartaecommerce.common.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMaxDiscountForProductService implements GetMaxDiscountForProductUseCase {

    private final LoadProductPort loadProductPort;
    private final LoadCouponPort loadCouponPort;
    private final CouponDiscountCalculator discountCalculator;
    private final DateTimeHolder dateTimeHolder;

    @Override
    public MaxDiscountResult getMaxDiscount(Long productId) {
        Product product = loadProductPort.getById(productId);
        Money productPrice = product.getPrice();

        if (productPrice.isZero()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot calculate discount for product with zero price. productId: " + productId
            );
        }

        // 상품에 적용 가능한 활성 쿠폰만 조회 (전체 + 카테고리 + 상품)
        List<Coupon> applicableCoupons = loadCouponPort.findApplicableCouponsForProduct(
            productId,
            product.getCategoryId(),
            dateTimeHolder.getCurrentDateTime()
        );

        if (applicableCoupons.isEmpty()) {
            return createZeroDiscountResult();
        }

        // min_order_amount를 만족하는 쿠폰만 필터링하고 최대 할인율/할인액을 계산
        return applicableCoupons.stream()
            .filter(coupon -> coupon.getMinOrderAmount().isLessThanEqual(productPrice))
            .filter(coupon -> coupon.getUsedCount() < coupon.getUsageLimit())
            .map(coupon -> {
                BigDecimal discountRate = discountCalculator.calculateDiscountRate(coupon, productPrice);
                Money discountAmount = discountCalculator.calculateDiscountAmount(coupon, productPrice);

                return new MaxDiscountResult(
                    discountRate,
                    coupon.getCouponId(),
                    coupon.getCouponName(),
                    coupon.getDiscountType().name(),
                    coupon.getDiscountValue().toString(),
                    discountAmount
                );
            })
            .max(Comparator.comparing(MaxDiscountResult::maxDiscountRate))
            .orElseGet(this::createZeroDiscountResult);
    }

    private MaxDiscountResult createZeroDiscountResult() {
        return new MaxDiscountResult(
            BigDecimal.ZERO,
            null,
            null,
            null,
            null,
            Money.zero()
        );
    }
}
