package com.spartaecommerce.coupon.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class CouponCodeGeneratorTest {

    private final CouponCodeGenerator generator = new CouponCodeGenerator();

    @Test
    @DisplayName("쿠폰 코드 생성 - 형식 확인")
    void generate_checkFormat() {
        // when
        String code = generator.generate();

        // then
        assertThat(code).isNotNull();
        assertThat(code).hasSize(14); // XXXX-XXXX-XXXX
        assertThat(code).matches("[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}");
    }

    @Test
    @DisplayName("쿠폰 코드 생성 - 여러 번 생성 시 다른 코드")
    void generate_multipleGenerations_differentCodes() {
        // when
        String code1 = generator.generate();
        String code2 = generator.generate();
        String code3 = generator.generate();

        // then
        assertThat(code1).isNotEqualTo(code2);
        assertThat(code2).isNotEqualTo(code3);
        assertThat(code1).isNotEqualTo(code3);
    }

    @Test
    @DisplayName("고유 쿠폰 코드 세트 생성 - 요청한 개수만큼 생성")
    void generateUniqueCodes_correctCount() {
        // given
        int count = 100;

        // when
        Set<String> codes = generator.generateUniqueCodes(count);

        // then
        assertThat(codes).hasSize(count);
    }

    @Test
    @DisplayName("고유 쿠폰 코드 세트 생성 - 모두 고유한 코드")
    void generateUniqueCodes_allUnique() {
        // given
        int count = 1000;

        // when
        Set<String> codes = generator.generateUniqueCodes(count);

        // then
        assertThat(codes).hasSize(count); // Set이므로 중복이 없어야 함
        assertThat(codes).allMatch(code -> code.matches("[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}"));
    }

    @Test
    @DisplayName("고유 쿠폰 코드 세트 생성 - 적은 개수")
    void generateUniqueCodes_smallCount() {
        // when
        Set<String> codes = generator.generateUniqueCodes(5);

        // then
        assertThat(codes).hasSize(5);
    }
}
