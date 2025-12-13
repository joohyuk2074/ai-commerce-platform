package com.spartaecommerce.coupon.domain.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * 쿠폰 코드 생성 도메인 서비스
 */
@Component
public class CouponCodeGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 고유한 쿠폰 코드를 생성합니다
     * 형식: XXXX-XXXX-XXXX (12자, 하이픈 포함 시 14자)
     *
     * @return 생성된 쿠폰 코드
     */
    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH + 2);

        for (int i = 0; i < CODE_LENGTH; i++) {
            if (i > 0 && i % 4 == 0) {
                code.append('-');
            }
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }

        return code.toString();
    }

    /**
     * 중복되지 않는 고유한 쿠폰 코드 세트를 생성합니다
     *
     * @param count 생성할 코드 개수
     * @return 생성된 쿠폰 코드 세트
     */
    public Set<String> generateUniqueCodes(int count) {
        Set<String> codes = new HashSet<>();

        while (codes.size() < count) {
            codes.add(generate());
        }

        return codes;
    }
}
