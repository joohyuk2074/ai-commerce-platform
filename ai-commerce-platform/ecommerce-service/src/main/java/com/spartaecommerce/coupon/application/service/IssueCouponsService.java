package com.spartaecommerce.coupon.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.coupon.application.dto.command.IssueCouponsCommand;
import com.spartaecommerce.coupon.domain.entity.Coupon;
import com.spartaecommerce.coupon.domain.entity.CouponUser;
import com.spartaecommerce.coupon.domain.port.in.IssueCouponsUseCase;
import com.spartaecommerce.coupon.domain.port.out.CouponCodeGeneratorPort;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponPort;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponUserPort;
import com.spartaecommerce.coupon.domain.port.out.SaveCouponUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 쿠폰 대량 발급 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IssueCouponsService implements IssueCouponsUseCase {

    private final LoadCouponPort loadCouponPort;
    private final LoadCouponUserPort loadCouponUserPort;
    private final SaveCouponUserPort saveCouponUserPort;
    private final CouponCodeGeneratorPort couponCodeGeneratorPort;

    @Override
    public List<Long> issue(IssueCouponsCommand command) {
        validateIssueCommand(command);

        Coupon coupon = loadCouponPort.getById(command.couponId());

        Set<String> uniqueCodes = generateUniqueCodesWithRetry(command.quantity());

        List<CouponUser> couponUsers = uniqueCodes.stream()
            .map(uniqueCode -> CouponUser.createNew(coupon.getCouponId(), uniqueCode))
            .toList();

        List<Long> savedIds = saveCouponUserPort.saveAll(couponUsers);

        log.info("쿠폰 대량 발급 완료 - 쿠폰ID: {}, 발급수량: {}", command.couponId(), savedIds.size());

        return savedIds;
    }

    private Set<String> generateUniqueCodesWithRetry(int quantity) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            Set<String> codes = couponCodeGeneratorPort.generateUniqueCodes(quantity);

            // N+1 문제 해결: 배치 조회로 한 번에 중복 확인
            Set<String> existingCodes = loadCouponUserPort.findExistingCodes(codes);
            codes.removeAll(existingCodes);

            if (codes.size() == quantity) {
                return codes;
            }

            int remaining = quantity - codes.size();
            Set<String> additionalCodes = couponCodeGeneratorPort.generateUniqueCodes(remaining);
            codes.addAll(additionalCodes);

            existingCodes = loadCouponUserPort.findExistingCodes(codes);
            codes.removeAll(existingCodes);

            if (codes.size() >= quantity) {
                return codes.stream().limit(quantity).collect(Collectors.toSet());
            }

            retryCount++;
        }

        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
            "중복되지 않는 쿠폰 코드 생성에 실패했습니다. 재시도 횟수 초과");
    }

    private void validateIssueCommand(IssueCouponsCommand command) {
        if (command.couponId() == null || command.couponId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "쿠폰 ID는 필수입니다.");
        }
        if (command.quantity() == null || command.quantity() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "발급 수량은 1개 이상이어야 합니다.");
        }
        if (command.quantity() > 10000) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                "한 번에 발급할 수 있는 최대 수량은 10,000개입니다.");
        }
    }
}
