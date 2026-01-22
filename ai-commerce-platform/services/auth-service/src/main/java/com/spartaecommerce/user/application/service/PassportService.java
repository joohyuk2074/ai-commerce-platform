package com.spartaecommerce.user.application.service;

import com.spartaecommerce.auth.domain.port.in.CreatePassportUseCase;
import com.spartaecommerce.auth.domain.port.out.JwtTokenPort;
import com.spartaecommerce.common.auth.Passport;
import com.spartaecommerce.common.auth.PassportBuilder;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Passport 생성 서비스
 *
 * Gateway로부터 JWT 토큰을 받아 Passport를 생성합니다.
 * Passport는 사용자 정보를 포함한 일회성 인증 토큰입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassportService implements CreatePassportUseCase {

    private final LoadUserPort loadUserPort;
    private final JwtTokenPort jwtTokenPort;

    /**
     * JWT 토큰으로부터 Passport 생성
     *
     * @param jwtToken JWT Access Token
     * @return Passport 객체
     */
    @Override
    public Passport createPassportFromToken(String jwtToken) {
        if (!jwtTokenPort.validateToken(jwtToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid JWT token");
        }

        Long userId = jwtTokenPort.getUserIdFromToken(jwtToken);

        User user = loadUserPort.getById(userId);

        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Deleted user");
        }

        return createPassport(user);
    }

    /**
     * 사용자 ID로부터 Passport 생성
     *
     * @param userId 사용자 ID
     * @return Passport 객체
     */
    public Passport createPassportByUserId(Long userId) {
        // 1. 사용자 정보 조회
        User user = loadUserPort.getById(userId);

        // 2. 탈퇴한 사용자 체크
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Deleted user");
        }

        // 3. Passport 생성
        return createPassport(user);
    }

    /**
     * User 엔티티로부터 Passport 생성
     */
    private Passport createPassport(User user) {
        // 권한 목록 생성
        String grade = user.getGrade().name();
        List<String> roles = List.of(grade);

        // 메타데이터 생성
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdAt", user.getCreatedAt());
        metadata.put("lastLoginAt", Instant.now());
        metadata.put("accountStatus", "ACTIVE");

        // Passport 빌더로 생성
        Passport passport = PassportBuilder.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .email(user.getEmail())
            .grade(grade)
            .roles(roles)
            .metadata(metadata)
            .ttl(Duration.ofHours(1))  // 1시간 유효
            .build();

        log.debug("Passport created for user: {} (ID: {}), passportId: {}",
            user.getUsername(), user.getUserId(), passport.passportId());

        return passport;
    }
}
