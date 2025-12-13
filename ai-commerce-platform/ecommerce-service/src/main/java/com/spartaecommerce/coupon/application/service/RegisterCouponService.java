package com.spartaecommerce.coupon.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.coupon.application.dto.command.RegisterCouponCommand;
import com.spartaecommerce.coupon.application.dto.result.CouponUserResult;
import com.spartaecommerce.coupon.domain.entity.CouponUser;
import com.spartaecommerce.coupon.domain.port.in.RegisterCouponUseCase;
import com.spartaecommerce.coupon.domain.port.out.LoadCouponUserPort;
import com.spartaecommerce.coupon.domain.port.out.SaveCouponUserPort;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RegisterCouponService implements RegisterCouponUseCase {

    private final LoadCouponUserPort loadCouponUserPort;
    private final SaveCouponUserPort saveCouponUserPort;

    @Override
    public CouponUserResult register(RegisterCouponCommand command) {
        validateRegisterCommand(command);

        try {
            CouponUser couponUser = loadCouponUserPort.getByCode(command.couponCode());

            couponUser.issue(command.userId());

            saveCouponUserPort.save(couponUser);

            log.info("쿠폰 등록 완료 - 쿠폰코드: {}, 사용자ID: {}", command.couponCode(), command.userId());

            return CouponUserResult.from(couponUser);
        } catch (OptimisticLockException e) {
            log.warn("쿠폰 등록 실패 - 동시성 충돌 (낙관적 락) - 쿠폰코드: {}, 사용자ID: {}",
                    command.couponCode(), command.userId(), e);
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED,
                    "이미 다른 사용자에게 발급된 쿠폰입니다.");
        } catch (DataIntegrityViolationException e) {
            log.warn("쿠폰 등록 실패 - 유니크 제약조건 위반 - 쿠폰코드: {}, 사용자ID: {}",
                    command.couponCode(), command.userId(), e);
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED,
                    "이미 다른 사용자에게 발급된 쿠폰입니다.");
        }
    }

    private void validateRegisterCommand(RegisterCouponCommand command) {
        if (command.couponCode() == null || command.couponCode().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "쿠폰 코드는 필수입니다.");
        }
        if (command.userId() == null || command.userId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용자 ID는 필수입니다.");
        }
    }
}
